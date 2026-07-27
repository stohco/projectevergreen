#!/usr/bin/env python3
"""
Parser for RICanonicalDatabase.java — extracts all 630 records
(CanonCharacter, CanonLocation, CanonArtifact, CanonTechnique) and
emits them as JSON for downstream TypeScript generation.

This is a faithful structural port: every field value is preserved
verbatim from the Java source (with CRON-69 corrections already
applied in the Java file).

Usage: python3 parse_canon.py <input.java> <output.json>
"""
import json
import re
import sys
from typing import Any, List, Tuple


class Tok:
    """Tokenizer for a subset of Java sufficient to parse this file."""
    def __init__(self, src: str):
        self.src = src
        self.i = 0
        self.n = len(src)

    def skip_ws_and_comments(self) -> None:
        while self.i < self.n:
            c = self.src[self.i]
            if c in ' \t\r\n':
                self.i += 1
            elif c == '/' and self.i + 1 < self.n and self.src[self.i + 1] == '/':
                while self.i < self.n and self.src[self.i] != '\n':
                    self.i += 1
            elif c == '/' and self.i + 1 < self.n and self.src[self.i + 1] == '*':
                self.i += 2
                while self.i + 1 < self.n and not (self.src[self.i] == '*' and self.src[self.i + 1] == '/'):
                    self.i += 1
                self.i += 2
            else:
                break

    def peek(self) -> str:
        self.skip_ws_and_comments()
        return self.src[self.i] if self.i < self.n else ''

    def next_token(self) -> str:
        self.skip_ws_and_comments()
        if self.i >= self.n:
            return ''
        c = self.src[self.i]
        if c == '"':
            j = self.i + 1
            buf = []
            while j < self.n:
                cj = self.src[j]
                if cj == '\\':
                    if j + 1 < self.n:
                        nxt = self.src[j + 1]
                        if nxt == 'n': buf.append('\n')
                        elif nxt == 't': buf.append('\t')
                        elif nxt == 'r': buf.append('\r')
                        elif nxt == '"': buf.append('"')
                        elif nxt == '\\': buf.append('\\')
                        elif nxt == "'": buf.append("'")
                        else: buf.append(nxt)
                        j += 2
                    else:
                        j += 1
                elif cj == '"':
                    j += 1
                    break
                else:
                    buf.append(cj)
                    j += 1
            self.i = j
            return 'STR:' + ''.join(buf)
        if c.isalpha() or c == '_' or c == '$':
            j = self.i
            while j < self.n and (self.src[j].isalnum() or self.src[j] in '_$.'):
                j += 1
            tok = self.src[self.i:j]
            self.i = j
            return tok
        if c.isdigit() or (c == '-' and self.i + 1 < self.n and self.src[self.i + 1].isdigit()):
            j = self.i + 1
            while j < self.n and (self.src[j].isdigit() or self.src[j] == '.'):
                j += 1
            tok = self.src[self.i:j]
            self.i = j
            return tok
        self.i += 1
        return c


class Parser:
    def __init__(self, src: str):
        self.tok = Tok(src)

    def expect(self, t: str) -> None:
        x = self.tok.next_token()
        if x != t:
            raise SyntaxError(f"Expected {t!r}, got {x!r} at pos {self.tok.i}")

    def parse_value(self) -> Any:
        t = self.tok.next_token()
        if t.startswith('STR:'):
            return t[4:]
        if t == 'null':
            return None
        if t == 'true':
            return True
        if t == 'false':
            return False
        if t and (t[0].isdigit() or (t[0] == '-' and len(t) > 1 and t[1].isdigit())):
            # Numeric — but make sure we're not seeing '5.' style confusion
            if '.' in t:
                return float(t)
            return int(t)
        # List.of / java.util.List.of — handle both 'List' (then expect .of)
        # and 'List.of' (already-consumed dotted form)
        if t in ('List', 'java.util.List'):
            self.expect('.')
            self.expect('of')
            self.expect('(')
            return self.parse_arg_list()
        if t in ('List.of', 'java.util.List.of'):
            self.expect('(')
            return self.parse_arg_list()
        if t == 'new':
            cls = self.tok.next_token()
            self.expect('(')
            args = self.parse_arg_list()
            return {'__class__': cls, 'args': args}
        # Enum types — CharType.X / LocType.X / ArtType.X / TechType.X
        # The tokenizer (with '.' in identifier chars) consumes the whole
        # dotted name as one token.
        for prefix in ('CharType.', 'LocType.', 'ArtType.', 'TechType.'):
            if t.startswith(prefix):
                return t[len(prefix):]
        # BuildingTheme.X, CanonRoom.RoomFunction.X, etc. — return last component
        if '.' in t and not t[0].isdigit():
            return t.split('.')[-1]
        return t

    def parse_arg_list(self) -> List[Any]:
        args: List[Any] = []
        if self.tok.peek() == ')':
            self.tok.next_token()
            return args
        while True:
            v = self.parse_value()
            args.append(v)
            nxt = self.tok.next_token()
            if nxt == ')':
                return args
            if nxt != ',':
                raise SyntaxError(f"Expected ',' or ')', got {nxt!r} at pos {self.tok.i}")


def extract_blocks(src: str, start_marker: str) -> List[Tuple[int, str]]:
    lines = src.split('\n')
    start_idx = -1
    for i, ln in enumerate(lines):
        if start_marker in ln:
            start_idx = i
            break
    if start_idx < 0:
        raise ValueError(f"start marker {start_marker!r} not found")
    after = '\n'.join(lines[start_idx:])
    m = re.search(r'List\.of\(\s*', after)
    if not m:
        raise ValueError(f"List.of( not found after {start_marker!r}")
    body_start = m.end()
    blocks: List[Tuple[int, str]] = []
    i = body_start
    n = len(after)
    while i < n:
        m = re.search(r'\bnew\s+(CanonCharacter|CanonLocation|CanonArtifact|CanonTechnique)\s*\(', after[i:])
        if not m:
            break
        offset = i + m.start()
        depth = 1
        j = offset + m.end()
        in_str = False
        in_line_comment = False
        in_block_comment = False
        while j < n and depth > 0:
            c = after[j]
            if in_line_comment:
                if c == '\n':
                    in_line_comment = False
                j += 1
                continue
            if in_block_comment:
                if c == '*' and j + 1 < n and after[j + 1] == '/':
                    in_block_comment = False
                    j += 2
                    continue
                j += 1
                continue
            if in_str:
                if c == '\\':
                    j += 2
                    continue
                if c == '"':
                    in_str = False
                j += 1
                continue
            if c == '/' and j + 1 < n and after[j + 1] == '/':
                in_line_comment = True
                j += 2
                continue
            if c == '/' and j + 1 < n and after[j + 1] == '*':
                in_block_comment = True
                j += 2
                continue
            if c == '"':
                in_str = True
                j += 1
                continue
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        if depth != 0:
            raise SyntaxError(f"Unbalanced parens starting at offset {offset}")
        block_src = after[offset:j + 1]
        line_no = start_idx + 1 + after[:offset].count('\n')
        blocks.append((line_no, block_src))
        i = j + 1
        while i < n and after[i] in ' \t\r\n':
            i += 1
        if i < n and after[i] == ',':
            i += 1
        elif i < n and after[i] == ')':
            break
        else:
            break
    return blocks


def parse_record(block_src: str) -> List[Any]:
    p = Parser(block_src)
    p.expect('new')
    cls = p.tok.next_token()
    p.expect('(')
    args = p.parse_arg_list()
    return args


def main() -> None:
    if len(sys.argv) != 3:
        print("Usage: parse_canon.py <input.java> <output.json>", file=sys.stderr)
        sys.exit(2)
    src_path = sys.argv[1]
    out_path = sys.argv[2]
    with open(src_path, 'r', encoding='utf-8') as f:
        src = f.read()

    result: dict[str, Any] = {
        'characters': [],
        'locations': [],
        'artifacts': [],
        'techniques': [],
    }
    markers = [
        ('characters', 'ALL_CHARACTERS = List.of('),
        ('locations', 'ALL_LOCATIONS = List.of('),
        ('artifacts', 'ALL_ARTIFACTS = List.of('),
        ('techniques', 'ALL_TECHNIQUES = List.of('),
    ]
    for key, marker in markers:
        blocks = extract_blocks(src, marker)
        print(f"  {key}: {len(blocks)} records", file=sys.stderr)
        for line_no, block in blocks:
            try:
                args = parse_record(block)
            except Exception as e:
                print(f"  ERROR parsing {key} block at line {line_no}: {e}", file=sys.stderr)
                print(f"  Block (first 300 chars): {block[:300]}", file=sys.stderr)
                raise
            result[key].append({'__line__': line_no, 'args': args})

    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    total = sum(len(v) for v in result.values())
    print(f"  Wrote {total} records to {out_path}", file=sys.stderr)


if __name__ == '__main__':
    main()

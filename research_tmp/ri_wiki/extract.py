#!/usr/bin/env python3
"""Extract clean wiki content from page_reader JSON dump."""
import json, sys, re, html as ihtml

def clean(html: str) -> str:
    # Drop scripts and styles
    html = re.sub(r'<script[^>]*>[\s\S]*?</script>', '', html, flags=re.I)
    html = re.sub(r'<style[^>]*>[\s\S]*?</style>', '', html, flags=re.I)
    # Drop fandom ads / sidebars / global nav / comments
    for sel in ['header','nav','footer','aside','noscript']:
        html = re.sub(rf'<{sel}\b[^>]*>[\s\S]*?</{sel}>', '', html, flags=re.I)
    html = re.sub(r'<!--[\s\S]*?-->', '', html)
    # Try to find the main content area (Fandom uses .mw-parser-output)
    m = re.search(r'<div[^>]*class="[^"]*mw-parser-output[^"]*"[^>]*>([\s\S]*?)(?:<div[^>]*class="[^"]*(?:page-footer|page-side-tools|global-navigation|mcf-wrapper))', html)
    if m:
        html = m.group(1)
    # Convert block tags to newlines
    html = re.sub(r'<(br|/p|/div|/li|/h[1-6]|/tr|/td|/th)[^>]*>', '\n', html, flags=re.I)
    html = re.sub(r'<li[^>]*>', '• ', html, flags=re.I)
    html = re.sub(r'<(td|th)[^>]*>', ' | ', html, flags=re.I)
    # Strip remaining tags
    html = re.sub(r'<[^>]+>', '', html)
    html = ihtml.unescape(html)
    # Collapse blank lines
    html = re.sub(r'[ \t]+', ' ', html)
    html = re.sub(r'\n{3,}', '\n\n', html)
    return html.strip()

def main(in_path, out_path):
    with open(in_path) as f:
        d = json.load(f)
    data = d.get('data', d)
    title = data.get('title','')
    url = data.get('url','')
    html = data.get('html','')
    txt = clean(html)
    out = f"# {title}\nURL: {url}\n\n{txt}\n"
    with open(out_path,'w') as f:
        f.write(out)
    print(f"Wrote {len(out)} chars to {out_path}")

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])

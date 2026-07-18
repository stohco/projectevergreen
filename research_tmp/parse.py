import json, re, sys, os

def load(path):
    try:
        with open(path) as f:
            data = json.load(f)
    except Exception as e:
        print(f"ERR loading {path}: {e}", file=sys.stderr)
        return ""
    # Try common shapes
    html = ""
    if isinstance(data, dict):
        if 'data' in data and isinstance(data['data'], dict):
            html = data['data'].get('html','') or data['data'].get('content','')
        if not html:
            html = data.get('html','') or data.get('content','') or data.get('text','')
        if not html:
            # Try dumping nested
            html = json.dumps(data)
    elif isinstance(data, list):
        html = "".join([str(x) for x in data])
    else:
        html = str(data)
    # Strip HTML
    text = re.sub(r'<script[^>]*>.*?</script>', ' ', html, flags=re.DOTALL|re.IGNORECASE)
    text = re.sub(r'<style[^>]*>.*?</style>', ' ', text, flags=re.DOTALL|re.IGNORECASE)
    text = re.sub(r'<[^>]+>', ' ', text)
    text = re.sub(r'&nbsp;', ' ', text)
    text = re.sub(r'&amp;', '&', text)
    text = re.sub(r'&lt;', '<', text)
    text = re.sub(r'&gt;', '>', text)
    text = re.sub(r'&quot;', '"', text)
    text = re.sub(r'\s+', ' ', text)
    return text

base = "/home/z/my-project/tool-results"
files = {
    "techniques": f"{base}/wl_techniques.json",
    "cult": f"{base}/wl_cult.json",
    "cultivation": f"{base}/wl_cultivation.json",
    "void": f"{base}/wl_void.json",
    "baidu_main": f"{base}/wl_baidu_main.json",
    "main": f"{base}/wl_main.json",
    "guards": f"{base}/wl_guards.json",
    "clones": f"{base}/wl_clones.json",
    "clones_baidu": f"{base}/wl_clones_baidu.json",
    "items": f"{base}/wl_items.json",
    "items2": f"{base}/wl_items2.json",
}

for key, path in files.items():
    if not os.path.exists(path):
        print(f"MISSING: {path}", file=sys.stderr)
        continue
    text = load(path)
    out_path = f"/home/z/my-project/forge-mod/research_tmp/{key}.txt"
    with open(out_path, 'w') as f:
        f.write(text)
    print(f"{key}: {len(text)} chars -> {out_path}")

# HTML files
html_files = {
    "situ_nan": f"{base}/situ_nan_baidu.html",
    "allseer": f"{base}/allseer.html",
}
for key, path in html_files.items():
    if not os.path.exists(path):
        print(f"MISSING: {path}", file=sys.stderr)
        continue
    with open(path) as f:
        html = f.read()
    text = re.sub(r'<script[^>]*>.*?</script>', ' ', html, flags=re.DOTALL|re.IGNORECASE)
    text = re.sub(r'<style[^>]*>.*?</style>', ' ', text, flags=re.DOTALL|re.IGNORECASE)
    text = re.sub(r'<[^>]+>', ' ', text)
    text = re.sub(r'&nbsp;', ' ', text)
    text = re.sub(r'&amp;', '&', text)
    text = re.sub(r'\s+', ' ', text)
    with open(f"/home/z/my-project/forge-mod/research_tmp/{key}.txt", 'w') as f:
        f.write(text)
    print(f"{key}: {len(text)} chars")

"""Descarga el catálogo actual de illojuan.tv y lo guarda como instantánea para el APK.

Uso: python tools/snapshot_catalog.py app/src/main/assets/catalog.json
Replica la lógica de CatalogParser.kt (útil para comprobar si la web ha cambiado).
"""
import json
import re
import sys
import urllib.request

BASE = "https://illojuan.tv"


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": "CineIlloJuanTV/1.0"})
    with urllib.request.urlopen(req, timeout=30) as r:
        return r.read().decode("utf-8")


def read_template_literal(js, i):
    out = []
    while i < len(js):
        c = js[i]
        if c == "\\" and i + 1 < len(js):
            n = js[i + 1]
            out.append({"\\": "\\", "`": "`", "$": "$", "n": "\n", "t": "\t"}.get(n, "\\" + n))
            i += 2
        elif c == "`":
            return "".join(out), i + 1
        else:
            out.append(c)
            i += 1
    return "".join(out), i


def parse_bundle(js):
    pos = 0
    marker = "JSON.parse(`"
    while (start := js.find(marker, pos)) >= 0:
        literal, pos = read_template_literal(js, start + len(marker))
        if literal.lstrip().startswith('[{"name"') and '"videos"' in literal:
            return json.loads(literal)
    raise SystemExit("No se encontró el catálogo en el bundle")


html = get(BASE + "/")
bundle = re.search(r"/assets/index-[A-Za-z0-9_\-]+\.js", html).group(0)
cats = parse_bundle(get(BASE + bundle))
print(f"{bundle}: {len(cats)} categorías, {sum(len(c['videos']) for c in cats)} vídeos")
with open(sys.argv[1], "w", encoding="utf-8") as f:
    json.dump(cats, f, ensure_ascii=False)

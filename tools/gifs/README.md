# GIF generator

Renders the GIFs in `docs/showcase/gifs/` with the library's own `DustEngine` (no device needed).

```sh
cd tools/gifs
python3 -m pip install pillow
python3 make.py
```

Needs JDK 17+ and Python 3 with Pillow. Output lands in `tools/gifs/out/`; copy the files to
`docs/showcase/gifs/` to update the README gallery. The scenes (cards, photo, screen, list) are drawn
in `make.py`, so edit them there.

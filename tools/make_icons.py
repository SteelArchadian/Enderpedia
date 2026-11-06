"""Draws the Enderpedia icons: a purple book with an ender eye on the cover."""
import sys
from PIL import Image

O = (24, 10, 34, 255)      # outline
C = (88, 40, 120, 255)     # cover
H = (128, 70, 168, 255)    # cover highlight
S = (56, 22, 80, 255)      # spine shade
P = (232, 224, 200, 255)   # pages
Q = (190, 180, 150, 255)   # page shade
G = (60, 160, 120, 255)    # eye green
L = (130, 220, 170, 255)   # eye light
K = (16, 40, 32, 255)      # pupil
_ = None
ROWS = [
    "________________",
    "__OOOOOOOOOOOO__",
    "__OSCCCCCCCCHO__",
    "__OSCCCCCCCCCO__",
    "__OSCCCGGGCCCO__",
    "__OSCCGLGGGCCO__",
    "__OSCGLGKGGGCO__",
    "__OSCGGKKKGGCO__",
    "__OSCGGGKGGGCO__",
    "__OSCCGGGGGCCO__",
    "__OSCCCGGGCCCO__",
    "__OSCCCCCCCCCO__",
    "__OSOOOOOOOOOOO_",
    "__OSPPPPPPPPPPO_",
    "__OOQQQQQQQQQQO_",
    "___OOOOOOOOOOOO_",
]
COL = {"O": O, "C": C, "H": H, "S": S, "P": P, "Q": Q, "G": G, "L": L, "K": K, "_": (0, 0, 0, 0)}

def icon():
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(ROWS):
        for x, ch in enumerate(row):
            im.putpixel((x, y), COL[ch])
    return im

out = sys.argv[1]
icon().save(out + "/src/main/resources/assets/enderpedia/textures/gui/button.png")
icon().resize((128, 128), Image.NEAREST).save(out + "/src/main/resources/enderpedia.png")

import subprocess, os, shutil, math
from PIL import Image, ImageDraw, ImageFont
BG=(20,18,32,255); PAD=70; N=36
F="/System/Library/Fonts/Helvetica.ttc"
def font(s,b=False): return ImageFont.truetype(F,s,index=1 if b else 0)
def lerp(a,b,t): return tuple(int(a[i]+(b[i]-a[i])*t) for i in range(3))
def grad(w,h,c1,c2,diag=True):
    im=Image.new("RGBA",(w,h)); px=im.load()
    for y in range(h):
        for x in range(w):
            t=(x/w+y/h)/2 if diag else y/h
            px[x,y]=lerp(c1,c2,t)+(255,)
    return im
def rounded(im,r):
    m=Image.new("L",im.size,0); ImageDraw.Draw(m).rounded_rectangle((0,0,im.width-1,im.height-1),r,fill=255)
    out=Image.new("RGBA",im.size,(0,0,0,0)); out.paste(im,(0,0),m); return out
def card(label,sub,c1=(124,92,255),c2=(255,110,160),w=360,h=200):
    im=rounded(grad(w,h,c1,c2),24); d=ImageDraw.Draw(im)
    if h<120:
        d.ellipse((16,18,64,66),fill=(255,255,255,255)); d.text((40,42),"D",font=font(26,True),fill=c1,anchor="mm")
        d.text((80,22),label,font=font(22,True),fill="white"); d.text((80,50),sub,font=font(15),fill=(255,255,255,210)); return im
    d.ellipse((24,24,72,72),fill=(255,255,255,255)); d.text((48,48),"D",font=font(26,True),fill=c1,anchor="mm")
    d.text((24,100),label,font=font(30,True),fill="white"); d.text((24,142),sub,font=font(18),fill=(255,255,255,210))
    return im
def photo(w=360,h=240):
    im=grad(w,h,(255,170,90),(70,40,140),diag=False); d=ImageDraw.Draw(im)
    d.ellipse((220,60,300,140),fill=(255,236,170,255))
    d.polygon([(0,h),(0,170),(70,120),(140,180),(210,110),(300,190),(w,150),(w,h)],fill=(40,28,80,255))
    d.polygon([(0,h),(0,200),(90,170),(180,215),(280,175),(w,210),(w,h)],fill=(24,16,52,255))
    return rounded(im,20)
def screen(w=220,h=400):
    im=rounded(grad(w,h,(30,40,90),(110,60,170),diag=False),30); d=ImageDraw.Draw(im)
    d.text((20,26),"Inbox",font=font(28,True),fill="white")
    for i in range(4):
        y=80+i*74; d.rounded_rectangle((16,y,w-16,y+62),14,fill=(255,255,255,40)); d.ellipse((28,y+14,62,y+48),fill=(255,255,255,200))
        d.rounded_rectangle((74,y+16,150,y+26),4,fill=(255,255,255,230)); d.rounded_rectangle((74,y+36,w-40,y+44),4,fill=(255,255,255,120))
    return im
def render(name,scene,direction="LEFT_TO_RIGHT",wind=25,scatter=0.35,dist=0.45):
    sp=f"scenes/{name}.png"; os.makedirs("scenes",exist_ok=True); scene.save(sp)
    od=f"frames/{name}"; shutil.rmtree(od,ignore_errors=True)
    subprocess.run(["../../gradlew","run","-q","--args",f"{sp} {od} {N} {PAD} {direction} {wind} {scatter} {dist}"],check=True,capture_output=True)
    return [Image.open(f"{od}/f{i:03d}.png") for i in range(N)]
def frame(layers,size):
    c=Image.new("RGBA",size,BG)
    for im,pos in layers: c.alpha_composite(im,pos)
    return c
def save(name,frames,durs):
    os.makedirs("out",exist_ok=True)
    rgb=[f.convert("RGB") for f in frames]
    samp=rgb[::6]; m=Image.new("RGB",(rgb[0].width,rgb[0].height*len(samp)))
    for i,f in enumerate(samp): m.paste(f,(0,i*rgb[0].height))
    pal=m.quantize(255,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE)
    fr=[f.quantize(palette=pal,dither=Image.Dither.NONE) for f in rgb]
    fr[0].save(f"out/{name}.gif",save_all=True,append_images=fr[1:],duration=durs,loop=0,disposal=1,optimize=False)
    print(name,os.path.getsize(f"out/{name}.gif")//1024,"KB")
def timeline(make,n=N):
    seq=[0]*10+list(range(n))+[n-1]*8+list(range(n-1,-1,-1))+[0]*14
    durs=[60 if i in(0,n-1) else 40 for i in seq]
    return [make(i) for i in seq],durs
def simple(name,scene,**kw):
    fr=render(name,scene,**kw); W,H=scene.width+2*PAD,scene.height+2*PAD
    f,d=timeline(lambda i:frame([(fr[i],(0,0))],(W,H))); save(name,f,d)
for dname,lbl in [("LEFT_TO_RIGHT","Left to right"),("RIGHT_TO_LEFT","Right to left"),("TOP_TO_BOTTOM","Top to bottom"),("BOTTOM_TO_TOP","Bottom to top"),("RANDOM","Random")]:
    simple("direction-"+dname.lower().replace("_","-"),card(lbl,"direction = "+dname),direction=dname)
simple("wind-scatter",card("Windblown","wind 90°, scatter 0.8",(40,160,170),(70,90,220)),wind=90,scatter=0.8,dist=0.6)
simple("image",photo())
simple("screen",screen(),wind=60)
# list dismiss
rows=[card(t,s,c1,c2,w=320,h=84) for t,s,c1,c2 in [("Write release notes","Today",(124,92,255),(255,110,160)),("Review pull request","Tomorrow",(40,160,170),(70,90,220)),("Tag version 0.1.0","This week",(240,150,60),(230,80,90))]]
def lst(i,fr):
    gap=16; W,H=320+2*PAD,3*84+2*gap+2*PAD; p=i/(N-1); slide=max(0,min(1,(p-0.55)/0.4)); slide=slide*slide*(3-2*slide)
    L=[(fr[i],(0,0))] if True else []
    y2=PAD+84+gap; y3=y2+84+gap; off=(84+gap)*slide
    return frame(L+[(rows[1],(PAD,int(y2-off))),(rows[2],(PAD,int(y3-off)))],(W,H))
fr=render("list-dismiss",rows[0],direction="LEFT_TO_RIGHT",wind=25,scatter=0.35,dist=0.6)
# first row sits at (PAD,PAD) inside the padded frame
W,H=320+2*PAD,3*84+2*16+2*PAD
def mk(i):
    base=Image.new("RGBA",(W,H),BG); 
    p=i/(N-1); slide=max(0,min(1,(p-0.55)/0.4)); slide=slide*slide*(3-2*slide); off=(84+16)*slide
    base.alpha_composite(rows[1],(PAD,int(PAD+100-off))); base.alpha_composite(rows[2],(PAD,int(PAD+200-off)))
    base.alpha_composite(fr[i],(0,0)); return base
f,d=timeline(mk); 
# restore phase should not re-slide; acceptable: reverse plays list sliding back
save("list-dismiss",f,d)

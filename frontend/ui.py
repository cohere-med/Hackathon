import tkinter
import tkinter.messagebox

top = tkinter.Tk()
top.geometry("400x200")

def directCallBack():
   tkinter.messagebox.showinfo( "Hello Python", "directCallBack")

def ambulanceCallBack():
   tkinter.messagebox.showinfo( "Hello Python", "ambulanceCallBack")
   
def readCallBack():
   tkinter.messagebox.showinfo( "Hello Python", "readCallBack")
   
w = tkinter.Label(top, text="Queens Hospital")
B = tkinter.Button(top, text ="Direct Admit", command = directCallBack, fg = "red")
C = tkinter.Button(top, text ="Ambulance Admit", command = ambulanceCallBack, fg = "red")
D = tkinter.Button(top, text ="Read Data", command = readCallBack, fg = "red")

w.pack();
B.pack()
C.pack()
D.pack()

top.mainloop()
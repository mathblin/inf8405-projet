import numpy as np
import cv2
    
class CamHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        print (self.path)
        if self.path.endswith('.mjpg'):
            self.send_response(200)
            self.send_header('Content-type','multipart/x-mixed-replace; boundary=--jpgboundary')
            self.end_headers()
            while(True):# infinite loop with no exit condition
                    rc,img = capture.read()
                    if not rc:
                        continue 
                    if rc:
                        #========send img to  the cv_frame() function for CV2 operations======
                        imgRGB = cv_frame(img) # contains all the opencv stuff i want to perform
                        #=============================================================

                        #imgRGB = cv2.cvtColor(img,cv2.COLOR_BGR2RGB)
                        r, buf = cv2.imencode(".jpg",imgRGB) 

                        self.wfile.write("--jpgboundary\r\n")
                        self.send_header('Content-type','image/jpeg')
                        self.send_header('Content-length',str(len(buf)))
                        self.end_headers()
                        self.wfile.write(bytearray(buf))
                        self.wfile.write('\r\n')
                        time.sleep(0.01)

                        k = cv2.waitKey(20)
                        if k == 27: 
                            break



            cv2.destroyAllWindows()
            capture.release()   

            return

        if self.path.endswith('.html') or self.path=="/":
            self.send_response(200)
            self.send_header('Content-type','text/html')
            self.end_headers()
            self.wfile.write('<html><head></head><body>')
            self.wfile.write('<img src="http://'+socket.gethostbyname(socket.gethostname())+':'+ str(port) +'/cam.mjpg"/>')
            self.wfile.write('</body></html>')
            return





def main():

    global capture, average
    capture = cv2.VideoCapture(0)

    capture.set(3,800)
    capture.set(4,600)


    while(1):

        server = HTTPServer(('',port),CamHandler)
        print ("server started on: ")
        print(socket.gethostbyname(socket.gethostname()))
        CamHandler.BaseHTTPServer.BaseHTTPRequestHandler("GET", (internalipaddress ,portnumber), CamHandler)
        server.handle_request()
        k = cv2.waitKey(20)

        if k == ord('q'):

            capture.release()
            server.socket.close() 
            print("server stopped")

            cv2.destroyAllWindows()
            break

    return



main()

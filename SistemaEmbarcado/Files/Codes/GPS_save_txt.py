import gps

# Listen on port 2947 (gpsd) of localhost
session = gps.gps("localhost", "2947")
session.stream(gps.WATCH_ENABLE | gps.WATCH_NEWSTYLE)
arq = open("/home/pi/gpsdata.txt", "w")

while True:
    try:
        report = session.next()
                # Wait for a 'TPV' report and display the current time
                # To see all report data, uncomment the line below
                # print report
        if report['class'] == 'TPV':
            if hasattr(report, 'time'):
                print "Time = " + report.time
                arq.write(report.time + "@")
            if hasattr(report, 'lat'):
                print "Latitude = " + str(report.lat)
                arq.write(str(report.lat) + "@")
            if hasattr(report, 'lon'):
                print "Longitude = " + str(report.lon)
                arq.write(str(report.lon))
            print ""
            arq.write("\n")
    except KeyError:
                pass
    except KeyboardInterrupt:
                quit()
    except StopIteration:
                session = None
                print "GPSD has terminated"

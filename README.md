# UdpRelay
Allow a remote UDP service to appear as a local service on an android device. A form of port forwarding.

Example: you have a remote service running on 203.0.113.10:1194.
This app will allow you to forward traffic so the service appears to be running on the local network
eg 192.168.49.130:1194

This will work even if your provider does not allow tethering. I will be blogging about this shortly.

The code is very embrionic. The IP addresses are currently hard-coded and you will need to edit the source for your needs:
Please ajust the following settings accoringly (in MainActivity.java)

 * SERVER_IP, SERVER_PORT - the address of the remote service
 * ANDROID_LOCAL_IP, ANDROID_LOCAL_PORT - the LAN address at which the service will appear locally 
 * CLIENT_IP, CLIENT_PORT - the address of the client which requires access to the service

TODO:
 * A user interface, including
   * Settings for the above
   * Error logging
   * License
   * Instruction manual
 * Support clients connecting from an arbitrary address
 * Support running as a "service" without needing to keep the main activity alive
 * An icon
 

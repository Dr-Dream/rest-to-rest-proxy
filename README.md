Small project for remote api debugging
To start
1) Compile and start gw wich creates an WS for agent connections and start accepting http trafic
2) Configure and start agent whic h will connect to WS of gw and binds to specific to agent endpoint
3) All traffic comming to that endpoint will be passed to agent without cahnges
4) Agent getting trffic cahanges host on incomming http request and passes it to binded local address.

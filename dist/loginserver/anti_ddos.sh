#!/bin/sh
IPT=/sbin/iptables

UNPRIPORTS="1024:65535"
INET_IFACE="eth0"

$IPT -A INPUT -i lo -j ACCEPT
$IPT -A OUTPUT -o lo -j ACCEPT
$IPT -A OUTPUT -o eth0 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 22 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp -m tcp --dport 2106 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 7000 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 80 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 3306 -j ACCEPT -s 127.0.0.1
$IPT -A INPUT -i eth0 -p tcp -m tcp --dport 7777 -j DROP
$IPT -I INPUT -i eth0 -p tcp --dport 2106 -m connlimit --connlimit-above 2 -j DROP 
$IPT -I INPUT -i eth0 -p tcp --dport 7777 -m connlimit --connlimit-above 5 -j DROP
$IPT -A INPUT -p icmp -i eth0 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 25 -j ACCEPT
$IPT -A INPUT -i eth0 -p tcp --dport 110 -j ACCEPT 
$IPT -A INPUT -i eth0 -p tcp -m tcp --tcp-flags FIN,SYN,ACK SYN -j REJECT --reject-with icmp-port-unreachable
$IPT -A INPUT -p icmp --icmp-type 8 -s 0/0
$IPT -A INPUT -p udp -m udp -i $INET_IFACE --dport $UNPRIPORTS --sport 53 -j ACCEPT
$IPT -A INPUT -p tcp -m tcp -i $INET_IFACE --dport 1024:65353 --sport 53 -j ACCEPT
$IPT -A INPUT -p tcp -m tcp -i $INET_IFACE --dport $UNPRIPORTS --sport 21 -j ACCEPT ! --syn
$IPT -A INPUT -p tcp -m tcp -m multiport -i $INET_IFACE --dport $UNPRIPORTS -j ACCEPT --sports 80,443 ! --syn
$IPT -A INPUT -p tcp -m tcp -i $INET_IFACE --dport $UNPRIPORTS --sport 25 -j ACCEPT

$IPT -P INPUT DROP
$IPT -P OUTPUT ACCEPT

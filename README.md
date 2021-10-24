# DNS_java
# Overview
dnsjava is an implementation of DNS in Java. It supports almost all defined record types (including the DNSSEC types), and unknown types. It can be used for queries, zone transfers, and dynamic updates. It includes a cache which can be used by clients, and an authoritative only server. It supports TSIG authenticated messages, partial DNSSEC verification, and EDNS0. It is fully thread safe.

dnsjava was started as an excuse to learn Java. It was useful for testing new features in BIND without rewriting the C resolver. It was then cleaned up and extended in order to be used as a testing framework for DNS interoperability testing. The high level API and caching resolver were added to make it useful to a wider audience. The authoritative only server was added as proof of concept.

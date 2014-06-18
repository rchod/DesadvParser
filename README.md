DesadvParser
============

A tool to parse a DESADV message and look for errors


![alt tag](https://lh3.googleusercontent.com/fJHRB7XsEhk-QCS-ozmzhH2zESiEgAt8rhZRjFR5hGVAOr8rZu3_lBUeJ_O6QL-90AA-k790wsQ)

it can detect the following errors:

- repeated segments 
- required segments not present
- wrong number qualifiants specified by Renault
- segments order
- UNT and UNZ check
- numeric and alphanumeric format
- DESADV header errors

logical errors:

- wrong number of GIR segments
- wrong total of packages

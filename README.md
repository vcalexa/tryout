## General ##

The service calculates commissions for transactions.

## Technical Design ##

It provides single endpoint POST `/transaction`.
It has implemented PostgreSQL as data storage. 
Unit and integration tests are enabled.


## Setup ##

Install Docker. 

Install make.

Run on your linux:

`make db app`

## Testing ##

Testing is possible via swagger. On your web browser enter on:
http://localhost:8080/swagger-ui/


Request body example:

{
  "date": "2021-01-01",
  "amount": "100.00",
  "currency": "EUR",
  "client_id": 42
}


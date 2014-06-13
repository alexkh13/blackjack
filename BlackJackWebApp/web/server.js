/**
 * Created by idmlogic on 07-Jun-14.
 */

var express = require("express");

var app = express();

app.use(express.static("."));

app.listen("6060");
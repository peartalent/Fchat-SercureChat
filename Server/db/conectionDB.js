const mysql = require("mysql");
const {
  DB_HOST,
  DB_USENAME,
  DB_PORT,
  DB_PASSWORD,
  DB_NAME,
} = require("../config/databaseConfig");
const connection = mysql.createConnection({
  host: DB_HOST,
  user: DB_USENAME,
  password: DB_PASSWORD,
  database: DB_NAME,
  port: DB_PORT,
  multipleStatements: true
});
const connectDB = function () {
  connection.connect((err) => {
    if (!err) {
      console.log("Database is connected!!!");
    } else {
      console.log("Database connect err!!!");
    }
  });
};
const closeDB = () => {
  connection.end((err) => {
    if (!err) {
      console.log("Database is closed!!!");
    }
  });
};

module.exports = {
  connectDB: connectDB,
  closeDB: closeDB,
  connection: connection,
};

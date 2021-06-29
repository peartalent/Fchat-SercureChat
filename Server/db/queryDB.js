const { connectDB, closeDB, connection } = require("./conectionDB");

exports.getAllUser = (callback) => {
  connectDB();
  connection.query("select * from user", (err, rs, fields) => {
    if (!err) {
      callback(rs);
    } else console.log(err);
  });
};

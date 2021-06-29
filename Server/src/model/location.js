const {connection} = require("../../db/conectionDB");
module.exports = {
    create: (location, callback) => {
        let sql = `INSERT INTO \`fchat\`.\`location\` (\`user_id\`, \`latitude\`, \`longitude\`, \`date_change\`) VALUES ('${location.user_id}', ${location.latitude}, ${location.longitude}, '${location.create_date}')`
        connection.query(sql,
            (err, rs) => {
                if (!err) {
                    callback({
                        code:200,
                        status:1
                    })
                } else {
                    callback({
                        code:200,
                        status:0
                    })
                }
            }
        );
    },
    update: function (location, callback) {
        let sql = `UPDATE \`fchat\`.\`location\` SET \`latitude\` = ${location.latitude}, \`longitude\` = ${location.longitude} WHERE \`user_id\` = '${location.user_id}'`;
        connection.query(sql,
            (err, rs) => {
                if (!err) {
                    callback({
                        code:200,
                        status:1
                    })
                } else {
                    callback({
                        code:200,
                        status:0
                    })
                }
            }
        );
    },
    get:(callback)=>{
        let sql = `SELECT * FROM location l, \`user\` u WHERE l.user_id = u.user_id`;
        connection.query(sql,
            (err, rs) => {
                if (!err) {
                    callback(rs)
                } else {
                    callback({
                        code:200,
                        status:0
                    })
                    console.log("get all location",err)
                }
            }
        );
    }
};
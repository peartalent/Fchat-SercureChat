const {connection} = require("../../db/conectionDB");
module.exports = {
  create: function (userId, contact) {
        let sql =`INSERT INTO \`fchat\`.\`contact\` (\`user_id\`, \`phone\`, \`name\`) VALUES ('${userId}', '${contact.phone}', '${contact.name}')`
        connection.query(sql, (err) => {
            if(err){
                //console.log(err)
            }
        });
    },
    getContactByUserIdAndPhone(userId,contact,callback){
        let sql =`INSERT INTO \`fchat\`.\`contact\` (\`user_id\`, \`phone\`, \`name\`) VALUES ('${userId}', '${contact.phone}', '${contact.name}')`
        connection.query(sql, (err, rs) => {
            if (!err) {
                callback(rs);
            } else console.log(err);
        });
    }
}
;
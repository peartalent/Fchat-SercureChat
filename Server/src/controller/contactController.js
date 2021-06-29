const query = require("../model/follow");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const typeData = require("../../config/type_data");
const typeNotification = require("../../config/type_notification");
const contactModel = require("../model/contact")
const userModel = require("../model/user")
const friendModel = require("../model/friend")
module.exports = {
    setContact: (req, res) => {
        let token = req.params.token
        let user = jwt.verify(token, KEY_SECRET);
        let str = req.body.contacts
        let contacts =JSON.parse(str);
        console.log("contact ",contacts)
        contacts.forEach(contact=>{
            if (contact.phone !== undefined){
                userModel.getByPhone(contact.phone,(rs)=>{
                    if (rs.length>0){
                        if(rs[0].user_id !==user.user_id){
                            friendModel.getByTwoId(user.user_id,rs[0].user_id,(rs2)=>{
                                // cần thêm không có trong block list
                                if (rs2.length <=0){
                                    friendModel.create(user.user_id,rs[0].user_id,require('moment')().format('YYYY-MM-DD HH:mm:ss'))
                                }
                            })
                        }
                    }
                })
                contactModel.create(user.user_id,contact)
            }
        })
    }
};
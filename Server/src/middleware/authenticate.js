const jwt = require('jsonwebtoken');
const {KEY_SECRET} = require("../../config/key");
module.exports ={
    login: (req,res)=>{
        try {
            let token = req.params.token;
            let user = jwt.verify(token, KEY_SECRET);
            console.log("login ne",user)
            res.json({
                status:1,
                code:1223,
                user:user
            })
        } catch (e){
            console.log("login fail","xxxxx")
            console.log(e)
            res.json({
                status:0,
                code:1223
            })
        }
    }
}
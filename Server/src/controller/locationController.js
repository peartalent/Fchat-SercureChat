const model = require("../model/location");
const {KEY_SECRET} = require("../../config/key");
const jwt = require('jsonwebtoken');
const user = require("../model/user");
const fs = require('fs')

// lấy khoảng cách
function distance(lat1, lat2, lon1, lon2) {
    lon1 = lon1 * Math.PI / 180;
    lon2 = lon2 * Math.PI / 180;
    lat1 = lat1 * Math.PI / 180;
    lat2 = lat2 * Math.PI / 180;

    let dlon = lon2 - lon1;
    let dlat = lat2 - lat1;
    let a = Math.pow(Math.sin(dlat / 2), 2)
        + Math.cos(lat1) * Math.cos(lat2)
        * Math.pow(Math.sin(dlon / 2), 2);

    let c = 2 * Math.asin(Math.sqrt(a));
    let r = 6371; //km
    return (c * r);
}

module.exports = {
    getUserByNearLocation: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        let location = {
            latitude: req.params.latitude,
            longitude: req.params.longitude,
            user_id: user.user_id,
        }
        var radius = req.params.radius
        model.get((rs) => {
            var users=[]
            rs.forEach(row=>{
                let u = {
                    user_id: row.user_id,
                    fullname: row.fullname,
                    avatar: row.avartar,
                    status: row.status,
                    last_online: row.last_online,
                    create_date: row.create_date,
                    public_key: row.public_key,
                    sex:row.sex,
                    token_client:row.token_client
                }
                let dis = distance(location.latitude,row.latitude,location.longitude,row.longitude)
                console.log("dis nè",dis,radius, u.user_id)
                if(dis <= radius && u.user_id !== location.user_id){
                    users.push(u)
                }
            })
            console.log("location near ",users)
            res.json(users)
        })
    },
    setLocation: (req, res) => {
        let user = jwt.verify(req.params.token, KEY_SECRET);
        let location = {
            latitude: req.body.latitude,
            longitude: req.body.longitude,
            user_id: user.user_id,
            create_date: require('moment')().format('YYYY-MM-DD HH:mm:ss')
        }
        console.log("location set ne", location)
        model.create(location, (rs) => {
            if (rs.status === 0) {
                model.update(location, (rs1) => {
                    console.log("location ", rs1)
                    res.json(rs1)
                })
            } else {
                res.json(rs)
            }
        })
    },


}

const express = require("express")
const app = express()
const router = express.Router()
app.use(express.urlencoded({extended: false}))
app.use(express.json())
const controller = require("../controller/userController")
const auth = require("../middleware/authenticate")
var multer, storage, path, crypto;
multer = require('multer')
path = require('path');
crypto = require('crypto');
var storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'public/image')
    },
    filename: function (req, file, cb) {
        return crypto.pseudoRandomBytes(16, function (err, raw) {
            if (err) {
                return cb(err);
            }
            console.log("====================file o main",raw)
            return cb(null, "" + (raw.toString('hex')) + (path.extname(file.originalname)));
        });
    }
});
router.post("/create/", controller.createUser)
router.get("/:token/user-by-phone/", controller.getByPhone)
router.get("/user-by-phone/:phone", controller.getMeByPhone)
router.put("/:token/update/name/:name", controller.updateNameById)
router.put("/:token/update/sex/:sex", controller.updateSexById)
router.get("/:token/user/logout", controller.logout)
router.get("/:token/user/login", auth.login)
router.post("/:token/update/avatar", multer({
    storage: storage
}).single('avatar'), controller.updateAvatarById)
// router.post("/:token/update/avatar", (req,res)=>{
//     console.log(req)
// })
router.get("/:token/user", controller.getUserByToken)
router.get("/client/:user_id", controller.getUserById)
router.get("/:token/check/:id2", controller.checkFriendOrFollower)
module.exports = router
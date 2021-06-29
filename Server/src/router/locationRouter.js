const express = require("express")
const router = express.Router()
const controller = require("../controller/locationController")

router.post("/:token/location/set", controller.setLocation)
router.get("/:token/location/:latitude/:longitude/:radius/get-user-near", controller.getUserByNearLocation)
module.exports = router
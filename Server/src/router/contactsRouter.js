const express = require("express")
const router = express.Router()
const controller = require("../controller/contactController")

router.post("/:token/contact/create", controller.setContact)
module.exports = router
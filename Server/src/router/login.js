var jwt = require('jsonwebtoken');

const express = require('express')
const router = express.Router();
const userModel = require('../model/user')
// get data
router.get('/', (req,res,next)=>{
})
// insert
router.post('/', (req,res,next)=>{
    let phone = req.body.phone
    userModel.get
})
// update
router.put('/', (req,res,next)=>{})
// delete
router.delete('/', (req,res,next)=>{})

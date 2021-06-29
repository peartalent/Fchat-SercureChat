const fs = require('fs')

const toBase64 = file => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
});
var url =  "../../public/image/6efd690020264448bcc8978317daa0a11624423421579.png"
var rs = fs.readFileSync(url, 'base64');
console.log(rs)

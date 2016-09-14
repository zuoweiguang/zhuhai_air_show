const CookieTool={
    addCookie(name,value,expireHours){
       let exdate=new Date()
        exdate.setTime(exdate.getTime()+expireHours*60*60*1000)
            document.cookie=name+"="+escape(value)+";expires="+exdate.toDateString()
    },
    getCookie(name){
        if (document.cookie){
            let arrCookie=document.cookie.split(";")
            if(arrCookie && arrCookie.length>0){
                for(let i=0;i<arrCookie.length;i++){
                    let arr=arrCookie[i].split("=")
                    if(arr[0].trim()==name.trim()){
                        return unescape(arr[1]||'')
                    }
                }
            }
        }
        return ""

    },
    deleteCookie(name){
        let exdate=new Date()
        exdate.setTime(exdate.getTime()-1)
        let cavl=""
        document.cookie=name+"="+cavl+";expires="+exdate.toDateString()
    }
}
export default CookieTool
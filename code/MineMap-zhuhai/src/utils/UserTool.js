import CookieTool from './CookieTool'
const UserTool = {
    getLoginUserId(){
        return CookieTool.getCookie("id")
    },
    getLoginUserName(){
        return CookieTool.getCookie("username")
    }

}

export default UserTool
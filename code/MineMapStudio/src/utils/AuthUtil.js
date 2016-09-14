import CookieTool from './CookieTool'
const AuthUtil = {

    id: CookieTool.getCookie("id"),
    username: CookieTool.getCookie("username"),
    password: CookieTool.getCookie("password"),
    email: CookieTool.getCookie("email"),
    token: CookieTool.getCookie("token"),
    autolchevalue:CookieTool.getCookie("autolchevalue"),

    isLogin() {
        let username=CookieTool.getCookie("username")
        let password=CookieTool.getCookie("password")
        return username && password != ""
    }
}

export default AuthUtil
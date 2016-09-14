class AuthService {
    constructor() {
        this.id = localStorage.userid
        this.username = localStorage.username
        this.password = ''
        localStorage.token = localStorage.token
    }

    isLogin() {
        return localStorage.username != ""
    }

    login(username, password, errmsg) {
        if (username == "admin" && password == "admin") {
            this.id = "admin";
            this.username = username;
            this.password = password;
            localStorage.token = "admin";
        } else {
            this.id = "";
            this.username = "";
            this.password = "";
            localStorage.token = "";
            errmsg = "用户名或密码错误!";
        }
        return this.isLogin();
    }

    logout() {
        this.id = "";
        this.username = "";
        this.password = "";
        localStorage.token = "";
    }
}

let authService = new AuthService;
export default authService;
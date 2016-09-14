# MineMapStudio
MineMapStudio

## 命令介绍:
* install:  
npm install  
安装package.json中的依赖组件

* start:
npm run start
启动webpack-dev-server,实时打包，翻译，替换组件，支持调试，开发时使用

* build:  
npm run build  
启动webpack，打包，翻译，压缩组件，不支持调试，生产环境使用

* test:  
npm run test  
启动测试

* lint:  
npm run lint  
检查代码风格


## 目录结构:
* app:产品代码根目录  
* build:打包后的代码根目录，运行build命令时会自动清理和创建  
* test:测试代码根目录  
* .babelrc:babel的配置文件，记录要使用的preset以及dev环境下启用hmr  
* .eslintrc:eslint的配置文件，记录要使用的代码风格规则  
* .gitignore:git的配置文件，记录要被忽略掉的文件和目录  
* karma.config.js:karma的配置文件，用于单元测试  
* package.json:npm配置文件，记录依赖项等信息  
* test.webpack.js:单元测试入口  
* webpack.config.js:dev环境下的webpack配置文件  
* webpack.production.config.js:production环境下的webpack配置文件

## 提交代码流程:
**在提交代码之前，请务必先运行lint和test命令，确保代码风格检查和单元测试都通过之后再提交**

## 相关资料:
* es6语法:http://es6.ruanyifeng.com  
* 代码风格:https://github.com/vikingmute/javascript
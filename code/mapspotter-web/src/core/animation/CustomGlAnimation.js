export default class CustomGlAnimation {
    constructor(map, options) {
        this.map = map
        this.options = options || {}
        this.painter = this.map.painter
        this.gl = this.painter.gl
        this.transform = this.painter.transform
        this.start = this.start.bind(this)
    }

    start() {
        this.painter.setDepthSublayer(0);
        this.painter.depthMask(false);
        this.gl.disable(this.gl.STENCIL_TEST);
        let gl  = this.gl
        var VSHADER_SOURCE = "" +
            "attribute vec4 pos;" +
            "uniform mat4 mx;" +
            "varying vec4 v_color;" +
            "attribute vec4 a_color;" +
            "void main(){" +
            "   gl_Position = mx * pos;" +
            "   v_color = a_color;" +
            "}";

        var FSHADER_SOURCE = "" +
            "precision lowp float;" +
            "varying vec4 v_color;" +
            "void main(){" +
            "gl_FragColor = v_color;" +
            "}" +
            "";

        var program = initShader(gl, VSHADER_SOURCE, FSHADER_SOURCE);

        var buffer = gl.createBuffer();
        var colorBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, buffer);

        var indexBuffer = gl.createBuffer();

        var buffer_data = new Float32Array([
            1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,  // 前面
            1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,  // 右面
            1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,  // 上面

            -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,   // 左面
            -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,  // 下面
            1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1  // 后面
        ]);

        var color_data = new Float32Array([
            .4, .4, 1.0, .4, .4, 1.0, .4, .4, 1.0, .4, .4, 1.0,  // 蓝色
            .4, 1.0, .4, .4, 1.0, .4, .4, 1.0, .4, .4, 1.0, .4,
            1.0, .4, .4, 1.0, .4, .4, 1.0, .4, .4, 1.0, .4, .4,

            1.0, 1.0, .4, 1.0, 1.0, .4, 1.0, 1.0, .4, 1.0, 1.0, .4,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
            .5, .5, 1.0, .5, .5, 1.0, .5, .5, 1.0, .5, .5, 1.0
        ]);

        var indices = new Uint8Array([
            0, 1, 2, 0, 2, 3,
            4, 5, 6, 4, 6, 7,
            8, 9, 10, 8, 10, 11,
            12, 13, 14, 12, 14, 15,
            16, 17, 18, 16, 18, 19,
            20, 21, 22, 20, 22, 23
        ]);

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, indices, gl.STATIC_DRAW);


        gl.bufferData(gl.ARRAY_BUFFER, buffer_data, gl.STATIC_DRAW);

        var posLocation = gl.getAttribLocation(program, 'pos');
        var aColorLocation = gl.getAttribLocation(program, 'a_color');
        var mxLocation = gl.getUniformLocation(program, 'mx');


        var BYTES_SIZE = buffer_data.BYTES_PER_ELEMENT;
        gl.vertexAttribPointer(posLocation, 3, gl.FLOAT, false, BYTES_SIZE * 3, 0);
        gl.enableVertexAttribArray(posLocation);


        var COLOR_BYTES_SIZE = color_data.BYTES_PER_ELEMENT;
        gl.bindBuffer(gl.ARRAY_BUFFER, colorBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, color_data, gl.STATIC_DRAW);
        gl.vertexAttribPointer(aColorLocation, 3, gl.FLOAT, false, COLOR_BYTES_SIZE * 3, 0);
        gl.enableVertexAttribArray(aColorLocation);


        // 透视投影矩阵
        var tsMX = getTS(30, 1, 1, 100);

        // 视图矩阵
        var vMX = getVMatrix2(3, 3, 7, 0, 0, 0);

        var mx = multiplyMatrix(tsMX, vMX);

        gl.uniformMatrix4fv(mxLocation, false, mx);

        gl.drawElements(gl.TRIANGLES, indices.length, gl.UNSIGNED_BYTE, 0);
    }
}
import React, { Component, PropTypes } from 'react'
import {Popover,OverlayTrigger} from 'react-bootstrap'
import InterpolateUtil from '../../utils/InterpolateUtil'
import env from '../../core/env'

export default class InterpolateValueItem extends Component {
    constructor(props, context) {
        super(props, context)
        this.addStop = this.addStop.bind(this)
        this.removeStop = this.removeStop.bind(this)
        this.handleChange = this.handleChange.bind(this)
        this.enableZoom = this.enableZoom.bind(this)
        this.orderStops = this.orderStops.bind(this)
        this.calculateInterpolateValue = this.calculateInterpolateValue.bind(this)
        this.calcInternalZoomAndValue = this.calcInternalZoomAndValue.bind(this)
        this.colorStrToRGB = this.colorStrToRGB.bind(this)
        this.colorRGBToHexStr = this.colorRGBToHexStr.bind(this)
        this.state = {valueType: 'number', interpolateValue: ""}
    }

    handleChange(e) {
        var contentItem = this.props.contentItem
        let newValue
        if (this.state.valueType == "object") {
            newValue = Object.assign({}, contentItem.defaultValue)
            if (e.target == this.refs["base"]) {
                newValue.base = parseFloat(this.refs.base.value)
            } else {
                let valLength = newValue.stops.length
                for (let i = 0; i < valLength; i++) {
                    if (e.target == this.refs["zoom" + i]) {
                        newValue.stops[i][0] = parseFloat(this.refs["zoom" + i].value)
                    } else if (e.target == this.refs["px" + i]) {
                        switch (this.props.type) {
                            case "number":
                                newValue.stops[i][1] = parseFloat(this.refs["px" + i].value)
                                break;
                            case "color":
                                newValue.stops[i][1] = this.refs["px" + i].value
                                break;
                            case "percent":
                                newValue.stops[i][1] = parseFloat(this.refs["px" + i].value) / 100.0
                                break;
                        }

                    }
                }
            }
            this.orderStops(newValue.stops)
            this.props.interpolateChange(newValue)

        } else {
            if (e.target == this.refs["interpolate-value"]) {
                switch (this.props.type) {
                    case "number":
                        this.props.interpolateChange(parseFloat(this.refs["interpolate-value"].value))
                        break;
                    case "percent":
                        this.props.interpolateChange(parseFloat(parseFloat(this.refs["interpolate-value"].value / 100.0).toFixed(2)))
                        break;
                    case "color":
                        this.props.interpolateChange(this.refs["interpolate-value"].value)
                        break;
                }
            }
        }
    }

    addStop(e) {
        var contentItem = this.props.contentItem
        let newValue
        newValue = Object.assign({}, contentItem.defaultValue)

        let zoomAndValue = this.calcInternalZoomAndValue()

        //这里需要限制一下zoom不能超过20吧
        if (zoomAndValue[0] > 20) {
            return
        }

        switch (this.props.type) {
            case "number":
                newValue.stops.push([zoomAndValue[0], zoomAndValue[1]])
                break;
            case "percent":
                newValue.stops.push([zoomAndValue[0], zoomAndValue[1]])
                break;
            case "color":
                newValue.stops.push([zoomAndValue[0], zoomAndValue[1]])
                break;
        }

        this.orderStops(newValue.stops)

        this.props.interpolateChange(newValue)
    }

    calcInternalZoomAndValue() {
        var contentItem = this.props.contentItem
        let newValue
        newValue = Object.assign({}, contentItem.defaultValue)

        let zoomNum = parseInt(env.map.getZoom())
        let zoomValue = this.refs["interpolate-value"].value
        let stopNumList = []
        for (let i = 0; i < newValue.stops.length; i++) {
            let stopNum = newValue.stops[i][0]
            stopNumList.push(stopNum)
        }
        while (true) {
            let hasSameNum = false;
            for (let i = 0; i < stopNumList.length; i++) {
                if (zoomNum == stopNumList[i]) {
                    hasSameNum = true
                }
            }
            if (!hasSameNum) {
                break
            } else {
                zoomNum++
            }
        }
        for (let i = 0; i < newValue.stops.length; i++) {
            if (zoomNum < newValue.stops[i][0] || i == (newValue.stops.length - 1)) {
                zoomValue = newValue.stops[i][1]
                break
            }
        }
        return [zoomNum, zoomValue]
    }

    enableZoom(e) {
        var contentItem = this.props.contentItem
        let newValue
        newValue = Object.assign({}, contentItem.defaultValue)

        switch (this.props.type) {
            case "number":
                newValue = {
                    base: 1,
                    stops: [
                        [0, parseFloat(this.refs["interpolate-value"].value)],
                        [20, parseFloat(this.refs["interpolate-value"].value)]
                    ]
                }
                this.setState({valueType: "object"})
                break;
            case "color":
                newValue = {
                    base: 1,
                    stops: [
                        [0, this.refs["interpolate-value"].value],
                        [20, this.refs["interpolate-value"].value]
                    ]
                }
                this.setState({valueType: "object"})
                break;
            case "percent":
                newValue = {
                    base: 1,
                    stops: [
                        [0, parseFloat(this.refs["interpolate-value"].value) / 100.0],
                        [20, parseFloat(this.refs["interpolate-value"].value) / 100.0]
                    ]
                }
                this.setState({valueType: "object"})
                break;
        }

        this.props.interpolateChange(newValue)
    }

    removeStop(e) {
        let contentItem = this.props.contentItem
        let target = e.target
        let stops
        if (this.state.valueType == "object") {
            stops = contentItem.defaultValue.stops
            let newValue = Object.assign({}, contentItem.defaultValue)
            for (let i = 0; i < stops.length; i++) {
                if (e.target == this.refs["stops" + i]) {
                    newValue.stops.splice(i, 1)
                }
            }

            if (newValue.stops.length > 1) {
                this.props.interpolateChange(newValue)
            } else {
                switch (this.props.type) {
                    case "number":
                        this.props.interpolateChange(parseFloat(this.refs["interpolate-value"].value))
                        this.setState({valueType: "number"})
                        break;
                    case "color":
                        this.props.interpolateChange(this.refs["interpolate-value"].value)
                        this.setState({valueType: "string"})
                        break;
                    case "percent":
                        this.props.interpolateChange(parseFloat(this.refs["interpolate-value"].value) / 100)
                        this.setState({valueType: "number"})
                        break;
                }
            }
        }
    }

    orderStops(value) {
        //用于对用户任意改动的zoom顺序重新排位
        for (let i = 0; i < value.length - 1; i++) {
            for (let j = i + 1; j < value.length; j++) {
                let value1 = value[i]
                let value2 = value[j]
                if (value1[0] > value2[0]) {
                    value[i] = value2
                    value[j] = value1
                }
            }
        }
    }

    colorStrToRGB(str) {
        let subStr = str.substring(1, str.length)
        let rgbList = [0, 0, 0], hexList = []
        if (subStr.length == 6) {
            hexList[0] = subStr.substr(0, 2)
            hexList[1] = subStr.substr(2, 2)
            hexList[2] = subStr.substr(4, 2)

            rgbList[0] = parseInt(hexList[0], 16)
            rgbList[1] = parseInt(hexList[1], 16)
            rgbList[2] = parseInt(hexList[2], 16)
        }
        return rgbList
    }

    colorRGBToHexStr(colorList) {
        var str = "#"
        for (let i = 0; i < colorList.length; i++) {
            if (colorList[i] <= 15) {
                str += "0" + colorList[i].toString(16)
            } else {
                str += colorList[i].toString(16)
            }
        }

        return str
    }

    calculateInterpolateValue() {
        let interpolateValue = "";
        let contentItem = this.props.contentItem
        let valueType = typeof contentItem.defaultValue;

        if (valueType == "object") {
            let stops = contentItem.defaultValue.stops
            let valLength = stops.length
            let zoom = env.map.getZoom()

            switch (this.props.type) {
                case "number":
                case "percent":
                    for (let i = 0; i < valLength; i++) {
                        if (zoom >= stops[i][0] && i < valLength - 1 && zoom <= stops[i + 1][0]) {
                            interpolateValue = InterpolateUtil.interpolateNumber(zoom, contentItem.defaultValue.base, stops[i][0], stops[i + 1][0], stops[i][1], stops[i + 1][1]).toFixed(2)
                        }
                    }
                    if (interpolateValue == "") {
                        if (zoom < stops[0][0]) {
                            interpolateValue = stops[0][1]
                        }
                        if (zoom > stops[valLength - 1][0]) {
                            interpolateValue = stops[valLength - 1][1]
                        }
                    }
                    break;
                case "color":
                    let startColorList = [], endColorList = []
                    for (let i = 0; i < valLength; i++) {
                        if (zoom >= stops[i][0] && i < valLength - 1 && zoom <= stops[i + 1][0]) {
                            startColorList = this.colorStrToRGB(stops[i][1])
                            endColorList = this.colorStrToRGB(stops[i + 1][1])
                            let colorRGB = [0, 0, 0]
                            for (let j = 0; j < 3; j++) {
                                colorRGB[j] +=
                                    parseInt(InterpolateUtil
                                        .interpolateNumber(zoom, contentItem.defaultValue.base, stops[i][0], stops[i + 1][0], startColorList[j], endColorList[j]).toFixed(0))
                            }
                            interpolateValue = this.colorRGBToHexStr(colorRGB)
                        }
                    }
                    if (interpolateValue == "") {
                        if (zoom < stops[0][0]) {
                            interpolateValue = stops[0][1]
                        }
                        if (zoom > stops[valLength - 1][0]) {
                            interpolateValue = stops[valLength - 1][1]
                        }
                    }
                    break;
            }
        } else {
            interpolateValue = contentItem.defaultValue
        }

        return interpolateValue
    }

    componentWillMount() {
        let valueType = typeof this.props.contentItem.defaultValue;
        this.setState({valueType: valueType})
    }

    render() {
        const { contentItem,interpolateChange,type} = this.props

        let base = <div>
            <div className="input-group input-group-sm">
                <input ref="base" type="number" className="form-control input-sm" placeholder="0"
                       style={{width:"145px",height:"24px"}}
                       min={0} max={1.99}
                       defaultValue={this.state.valueType == 'object'? contentItem.defaultValue.base:1}
                       onChange={this.handleChange}/>
                <span className="input-group-addon small"
                      style={{width:"20px",height:"12px",lineHeight:"12px"}}>base</span>
            </div>
            <label style={{fontSize:"12px",color:"#6c6c6c",fontWeight:"normal"}}>数值随比例尺变化率,1为线性.</label>
        </div>

        let stops = this.state.valueType == 'object' && typeof contentItem.defaultValue == 'object' ? contentItem.defaultValue.stops.map((value, i)=> {
            return <div key={i+":"+value[0]+":"+value[1]}>
                <div className="input-group input-group-sm" style={{display:"inline-block"}}>
                    <input ref={"zoom"+i} type="number" className="form-control input-sm" placeholder="0"
                           style={{width:"70px",height:"24px",borderRadius:"3px"}} min={0}
                           defaultValue={value[0]} onChange={this.handleChange}/>
                </div>
                <div className="input-group input-group-sm" style={{display:"inline-block",marginLeft:"5px"}}>
                    <input ref={"px"+i} type={type == "percent"?"number":type} className="form-control input-sm"
                           placeholder="0"
                           style={{width:"80px",height:"24px",padding:"2px 10px",borderRadius:"3px"}} min={type == "number"?0:null}
                           defaultValue={this.props.type == "percent"?value[1]*100:value[1]}
                           onChange={this.handleChange}/>
                </div>
                <button ref={"stops"+i} className="btn btn-default btn-sm glyphicon glyphicon-remove"
                        style={{height: "24px",lineHeight: "12px",marginTop:"-18px",marginLeft: "5px"}}
                        onClick={this.removeStop}></button>
            </div>
        }) : null

        let add = <div style={{color:"#3A8DEE",cursor:"pointer"}} onClick={this.addStop}>
            <span className="glyphicon glyphicon-plus"></span> Add stop
        </div>

        let enableZoomButton = <div>
            <button className="btn btn-default" style={{height:"24px",lineHeight:"12px"}} onClick={this.enableZoom}>
                依据比例尺设定数值
            </button>
        </div>

        let title = ""
        switch (type) {
            case "color":
                title = "修改颜色"
                break
            case "number":
                title = "修改线宽"
                break
            case "percent":
                title = "修改透明度"
                break
        }

        let overlay = this.state.valueType == 'object' ?
            <Popover id={contentItem.id} title={title}>{base} {stops} {add}</Popover> :
            <Popover id={contentItem.id} title={title}>{enableZoomButton}</Popover>


        return <div className="content-item" style={contentItem.style}>
            <div className="item-header">{contentItem.title}</div>
            <div className="item-body">
                <div className="input-group input-group-sm">
                    <input ref="interpolate-value" type={type == "percent"?"number":type} className="form-control input-sm" placeholder="0" style={{padding:"2px 10px"}}
                           onChange={this.handleChange}
                           disabled={this.state.valueType == 'object' ?"disabled":null}
                           value={this.props.type == 'percent' ? (this.calculateInterpolateValue()*100):this.calculateInterpolateValue()}/>
                    <OverlayTrigger trigger="click" rootClose placement="right" overlay={overlay}>
                        <span className="input-group-addon btn btn-default">
                            <span className="glyphicon glyphicon-menu-hamburger" style={{height:"12px"}}></span>
                        </span>
                    </OverlayTrigger>
                </div>
            </div>
        </div>

    }
}

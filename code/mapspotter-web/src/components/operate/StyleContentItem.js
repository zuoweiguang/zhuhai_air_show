import React , {Component , PropTypes} from 'react'
import {Popover,OverlayTrigger} from 'react-bootstrap'
import InterpolateValueItem from './InterpolateValueItem'
import createFetch from '../../utils/fetch'

export default class StyleContentItem extends Component {
    constructor(props, context) {
        super(props, context)
        this.handleChange = this.handleChange.bind(this)
        this.handleBtnClick = this.handleBtnClick.bind(this)
        this.buildJSONString = this.buildJSONString.bind(this)
        this.handleInterpolateChange = this.handleInterpolateChange.bind(this)
        this.renderIcon = this.renderIcon.bind(this)
        this.state = {icons: null, icon: null}
    }

    handleInterpolateChange(interpolateValue) {
        const contentItem = this.props.contentItem
        this.props.handleStyleChange(contentItem.id, interpolateValue)
    }

    handleChange(e) {
        e.preventDefault()
        const contentItem = this.props.contentItem
        let target = this.refs[contentItem.id] || e.target
        let evalValue
        console.log(target.defaultValue)
        switch (contentItem.type) {
            case "radio-group":
                if (target.defaultValue == "true") {
                    this.props.handleStyleChange(contentItem.id, true)
                } else if (target.defaultValue == "false") {
                    this.props.handleStyleChange(contentItem.id, false)
                } else {
                    this.props.handleStyleChange(contentItem.id, "" + target.defaultValue)
                }

                break
            case "select":
                if (contentItem.id == 'text-font') {
                    this.props.handleStyleChange(contentItem.id, ['微软雅黑', '宋体'])
                } else {
                    this.props.handleStyleChange(contentItem.id, target.value)
                }
                break
            case "multiselect":
                let multiValueList = []
                for (let i = 0; i < target.options.length; i++) {
                    let option = target.options[i]
                    if (option.selected) {
                        multiValueList.push(option.value)
                    }
                }
                this.props.handleStyleChange(contentItem.id, multiValueList)
                break
            case "select-btn-group":
                this.props.handleStyleChange(contentItem.id, target.value)
                break
            case "input-btn-group":
                this.props.handleStyleChange(contentItem.id, target.value)
                break
            case "input-group":
                let inputX = this.refs["input-x"]
                let inputY = this.refs["input-y"]
                if (inputX.value && inputY.value) {
                    this.props.handleStyleChange(contentItem.id, [parseFloat(inputX.value), parseFloat(inputY.value)])
                }
                break;
            case "text":
                let dataJSON
                try {
                    if (contentItem.id == 'icon-text-fit-padding') {
                        this.props.handleStyleChange(contentItem.id, [1, 1, 1, 1])
                    } else {
                        this.props.handleStyleChange(contentItem.id, new String(target.value || ''))
                    }
                } catch (e) {
                }
                break
            case "icon":
                if (this.refs[contentItem.id] == e.target) {

                    if (e.target.value.length == 0) {
                        this.props.handleStyleChange(contentItem.id, null)
                    } else {
                        this.props.handleStyleChange(contentItem.id, e.target.value)
                    }
                } else {
                    this.props.handleStyleChange(contentItem.id, e.target.title)
                }
                break
            default:
                if (contentItem.percent) {
                    evalValue = parseInt(contentItem.max) === 100 ? parseFloat((parseInt(target.value) / 100.0).toFixed(2)) : parseInt(target.value);
                } else {
                    evalValue = target.value.length == 7 ? target.value : parseInt(target.value)
                }
                this.props.handleStyleChange(contentItem.id, evalValue)
        }
    }

    handleBtnClick(e) {
        e.preventDefault()
        const contentItem = this.props.contentItem
        let target = this.refs[contentItem.id] || e.target
        let evalValue
        console.log(target.defaultValue)
        switch (contentItem.type) {
            case "select-btn-group":
                this.props.handleStyleBtnClick(contentItem.id, target.value)
                break;
            case "input-btn-group":
                this.props.handleStyleBtnClick(contentItem.id, target.value)
                break;

            default:
        }
    }

    buildJSONString(str) {
        return "{\"data\":" + str + "}"
    }

    renderIcon() {
        if (!this.state.icons) {
            let icons = []
            return createFetch("../app/sprite.json", "GET").then(response =>
                response.json()
            ).then(res => {
                for (var k in res) {
                    let obj = res[k]
                    let icon = <div key={k}>
                        <a className="operate-style-patten-icon" title={k} onClick={this.handleChange}
                           style={{backgroundPosition:-(obj.x) + 'px '+(-obj.y) + 'px',width:obj.width,height:obj.height}}></a>
                    </div>
                    icons.push(icon)
                }
                this.setState({icons: icons})
            }).then(()=> {
                return icons
            })
        }

    }

    render() {
        const {contentItem,handleStyleChange} = this.props;

        let renderRadio = contentItem.type == "radio-group" && contentItem.inputItems ? contentItem.inputItems.map((radio, i)=> {
            return <label key={contentItem.id+Math.random()*10} className="radio-inline">
                <input ref={contentItem.id+""+i} type="radio" value={radio.value}
                       defaultChecked={contentItem.defaultValue==radio.value} onChange={this.handleChange}/>
                <span>{radio.title}</span>
            </label>
        }) : null;

        let renderSelectOption = (contentItem.type == "select" || contentItem.type == 'multiselect' || contentItem.type == "select-btn-group") && contentItem.inputItems ? contentItem.inputItems.map((option, i)=> {
            return <option key={option.value} value={option.value}>{option.title}</option>
        }) : null;

        switch (contentItem.type) {
            case "number":
            case "color":
            case "text":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <input ref={contentItem.id} className="form-control input-sm" onChange={this.handleChange}
                               type={contentItem.type} defaultValue={contentItem.defaultValue}
                               disabled={contentItem.disabled}/>
                    </div>
                </div>
            case "icon":
                this.renderIcon()
                let overlay = <Popover id={contentItem.id} title={"图标选择"}>
                    <div style={{width:"250px",overflow:"auto"}}>{this.state.icons}</div>
                </Popover>
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <OverlayTrigger trigger="click" rootClose placement="right" overlay={overlay}>
                            <input ref={contentItem.id} className="form-control input-sm"
                                   type="text" onChange={this.handleChange}
                                   value={contentItem.defaultValue} disabled={contentItem.disabled}/>
                        </OverlayTrigger>
                    </div>
                </div>
            case "interpolate-number":
                return <InterpolateValueItem type="number" contentItem={contentItem}
                                             interpolateChange={this.handleInterpolateChange}/>
            case "interpolate-color":
                return <InterpolateValueItem type="color" contentItem={contentItem}
                                             interpolateChange={this.handleInterpolateChange}/>
            case "interpolate-percent":
                return <InterpolateValueItem type="percent" contentItem={contentItem}
                                             interpolateChange={this.handleInterpolateChange}/>
            case "interpolate-dasharray":
                return <InterpolateValueItem type="percent" contentItem={contentItem}
                                             interpolateChange={this.handleInterpolateChange}/>
            case "textarea":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <textarea ref={contentItem.id} className="form-control" rows="2"
                                  disabled={contentItem.disabled} value={contentItem.value}
                                  onChange={this.handleChange}/>
                    </div>
                </div>
            case "input-group":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <div className="input-group input-group-sm">
                            <input ref="input-x" type="number" className="form-control input-sm" placeholder="0"
                                   onChange={this.handleChange}
                                   disabled={contentItem.disabled}
                                   defaultValue={contentItem.inputItems[0].value}/>
                            <span className="input-group-addon">{contentItem.inputItems[0].title}</span>
                        </div>
                        <div className="input-group input-group-sm">
                            <input ref="input-y" type="number" className="form-control input-sm" placeholder="0"
                                   onChange={this.handleChange}
                                   disabled={contentItem.disabled}
                                   defaultValue={contentItem.inputItems[1].value}/>
                            <span className="input-group-addon">{contentItem.inputItems[1].title}</span>
                        </div>
                    </div>
                </div>
            case "input-btn-group":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <div className="input-group input-group-sm">
                            <input ref={contentItem.id} type="text" className="form-control input-sm" placeholder="0"
                                   onChange={this.handleChange}
                                   disabled={contentItem.disabled}
                                   defaultValue={contentItem.inputItems.value}/>
                            <span className="input-group-btn"><button
                                className={"btn btn-default btn-sm glyphicon "+contentItem.btnIcon}
                                style={{height: "24px",lineHeight: "12px",marginTop:"-1px"}}
                                title={contentItem.btnTitle}
                                onClick={this.handleBtnClick}></button></span>
                        </div>
                    </div>
                </div>
            case "radio-group":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <div id={contentItem.id} className="radio-group">
                            {renderRadio}
                        </div>
                    </div>
                </div>
            case "select":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <select ref={contentItem.id} className="form-control form-control-sm"
                                disabled={contentItem.disabled}
                                defaultValue={contentItem.defaultValue}
                                onChange={this.handleChange}
                        >
                            {renderSelectOption}
                        </select>
                    </div>
                </div>
            case "multiselect":
                return <div className="content-item"
                            style={Object.assign(contentItem.style,{height:(renderSelectOption.length*17+8)+'px'})}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <select ref={contentItem.id} className="form-control form-control-sm"
                                style={{height:(renderSelectOption.length*17+2)+'px'}}
                                disabled={contentItem.disabled}
                                multiple="multiple"
                                defaultValue={contentItem.defaultValue}
                                onChange={this.handleChange}
                        >
                            {renderSelectOption}
                        </select>
                    </div>
                </div>
            case "select-btn-group":
                return <div className="content-item" style={contentItem.style}>
                    <div className="item-header">{contentItem.title}</div>
                    <div className="item-body">
                        <div className="input-group input-group-sm">
                            <select ref={contentItem.id} className="form-control form-control-sm"
                                    disabled={contentItem.disabled}
                                    defaultValue={contentItem.defaultValue}
                                    onChange={this.handleChange}
                            >
                                {renderSelectOption}
                            </select>
                            <span className="input-group-btn"><button
                                className={"btn btn-default btn-sm glyphicon "+contentItem.btnIcon}
                                style={{height: "24px",lineHeight: "12px",marginTop:"-1px"}}
                                title={contentItem.btnTitle}
                                onClick={this.handleBtnClick}></button></span>
                        </div>
                    </div>
                </div>
        }
    }
}
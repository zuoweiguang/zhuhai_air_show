import React, { Component, PropTypes } from 'react'
import env from '../../core/env'
import operateHandler from '../../middleware/handler/operateHandler'
import icon2d from '../../images/map/3d/2d.png'
import icon3d from '../../images/map/3d/3d.png'

export default class MapControlToolbar extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {expand: false}

        this.onToolbarCollapsePanelClick = this.onToolbarCollapsePanelClick.bind(this)
        this.onHandleCtrlValueChange = this.onHandleCtrlValueChange.bind(this)
        this.onHandle3DShowClick = this.onHandle3DShowClick.bind(this)
        this.onCityListModelOpen = this.onCityListModelOpen.bind(this)
    }

    onToolbarCollapsePanelClick(e) {
        e.preventDefault()
        if (this.state.expand) {
            this.setState({expand: false})
        } else {
            this.setState({expand: true})
        }
    }

    onHandleCtrlValueChange(e) {
        e.preventDefault()
        const id = e.target.id
        let value = e.target.value
        switch (id) {
            case "zoomCtrlInput":
                if (isNaN(value)) {
                    document.execCommand('undo')
                    break
                }
                if (value < 1) {
                    value = 3
                    this.refs.zoomCtrlInput.value = value
                }
                if (value > 17) {
                    value = 17
                    this.refs.zoomCtrlInput.value = value
                }
                value = Number.parseFloat(value)
                this.props.actions.solutionPropertyModifyAction({id: this.props.solution.id, zoom: value})
                env.map.setZoom(value)
                break
            case "bearingCtrlInput":
                if (isNaN(value)) {
                    document.execCommand('undo')
                    break
                }
                if (value < -180) {
                    value = -180
                    this.refs.bearingCtrlInput.value = value
                }
                if (value > 360) {
                    value = 360
                    this.refs.bearingCtrlInput.value = value
                }
                value = Number.parseFloat(value)
                this.props.actions.solutionPropertyModifyAction({id: this.props.solution.id, bearing: value})
                env.map.setBearing(value)
                break
            case "pitchCtrlInput":
                if (isNaN(value)) {
                    document.execCommand('undo')
                    break
                }
                if (value < 0) {
                    value = 0
                    this.refs.pitchCtrlInput.value = value
                }
                if (value > 60) {
                    value = 60
                    this.refs.pitchCtrlInput.value = value
                }
                value = Number.parseFloat(value)
                this.props.actions.solutionPropertyModifyAction({id: this.props.solution.id, pitch: value})
                env.map.setPitch(value)
                break
            case "lngCtrlInput":
                if (isNaN(value)) {
                    document.execCommand('undo')
                    break
                }
                let center0 = env.map.getCenter()
                let center00 = [Number.parseFloat(value), center0.lat]
                this.props.actions.solutionPropertyModifyAction({id: this.props.solution.id, center: center00})
                env.map.setCenter(center00)
                break
            case "latCtrlInput":
                if (isNaN(value)) {
                    document.execCommand('undo')
                    break
                }
                let center1 = env.map.getCenter()
                let center11 = [center1.lng, Number.parseFloat(value)]
                this.props.actions.solutionPropertyModifyAction({id: this.props.solution.id, center: center11})
                env.map.setCenter(center11)
                break
            default:
        }
    }

    onHandle3DShowClick(e) {
        e.preventDefault()
        const solution = this.props.solution
        if (solution.pitch > 0) {
            this.props.actions.solutionPropertyModifyAction({id: solution.id, pitch: 0})
            env.map.setPitch(0)

            for (let i = 0; i < this.props.solution.layers.length; i++) {
                let aLayer = this.props.solution.layers[i];
                if (aLayer.paint["extrusion-min-height"] != -1 && aLayer.type == "extrusion") {
                    env.map.setPaintProperty(aLayer.id, "extrusion-min-height", -2)
                    this.props.actions.solutionLayerPaintModify(aLayer, "extrusion-min-height", -2)
                }
            }
        } else {
            this.props.actions.solutionPropertyModifyAction({id: solution.id, pitch: 60})
            env.map.setPitch(60)

            for (let i = 0; i < this.props.solution.layers.length; i++) {
                let aLayer = this.props.solution.layers[i];
                if (aLayer.paint["extrusion-min-height"] != -1 && aLayer.type == "extrusion") {
                    env.map.setPaintProperty(aLayer.id, "extrusion-min-height", 0)
                    this.props.actions.solutionLayerPaintModify(aLayer, "extrusion-min-height", 0)
                }
            }
        }
    }

    onCityListModelOpen(e) {
        e.preventDefault()
        this.props.mapControllCityPanelVisibleAction(true)
    }

    render() {
        const {solution,actions} = this.props
        const {zoom,bearing,pitch,center} = solution
        const expendStyle = this.state.expand ? {display: 'block'} : {display: 'none'}
        const collapseIconClass = this.state.expand ? 'glyphicon glyphicon-hand-up' : 'glyphicon glyphicon-hand-down'
        return <div className="map-control-toolbar-container">
            <div className="toolbar-collapse-container">
                <div className="toolbar-collapse" onClick={this.onToolbarCollapsePanelClick}>
                    <span className={collapseIconClass}></span>
                </div>
            </div>
            <div className="toolbar-expand-container" style={expendStyle}>
                <div className="toolbar-panel">
                    <div className="toolbar-item">
                        <div className="item-header">缩放级别</div>
                        <div className="item-content"><input ref="zoomCtrlInput" id="zoomCtrlInput"
                                                             className="form-control input-sm" type="text"
                                                             value={zoom}
                                                             onChange={this.onHandleCtrlValueChange}/></div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-header">旋转角度</div>
                        <div className="item-content"><input ref="bearingCtrlInput" id="bearingCtrlInput"
                                                             className="form-control input-sm"
                                                             type="text"
                                                             value={bearing}
                                                             onChange={this.onHandleCtrlValueChange}/></div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-header">俯仰角度</div>
                        <div className="item-content"><input ref="pitchCtrlInput" id="pitchCtrlInput"
                                                             className="form-control input-sm" type="text"
                                                             value={pitch}
                                                             onChange={this.onHandleCtrlValueChange}/></div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-header">经度坐标</div>
                        <div className="item-content"><input ref="lngCtrlInput" id="lngCtrlInput"
                                                             className="form-control input-sm" type="text"
                                                             value={center?center[0]:"0"}
                                                             onChange={this.onHandleCtrlValueChange}/>
                        </div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-header">纬度坐标</div>
                        <div className="item-content"><input ref="latCtrlInput" id="latCtrlInput"
                                                             className="form-control input-sm" type="text"
                                                             value={center?center[1]:"0"}
                                                             onChange={this.onHandleCtrlValueChange}/>
                        </div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-header">立体展示</div>
                        <div className="item-content">
                            <div className="icon-3d-box"><img width="24" height="24" src={pitch>0?icon3d:icon2d}
                                                              ref="icon3dImg"
                                                              onClick={this.onHandle3DShowClick}/></div>
                        </div>
                    </div>
                    <div className="toolbar-item">
                        <div className="item-btn-box">
                        <span type="button" className="btn btn-primary btn-sm"
                              onClick={this.onCityListModelOpen}>选择城市</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}

MapControlToolbar.propTypes = {
    solution: PropTypes.object.isRequired
}


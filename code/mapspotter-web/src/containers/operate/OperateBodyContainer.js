import '../../scss/operate/operateBody.scss'
import '../../scss/operate/operateMenu.scss'
import '../../scss/operate/operateLayer.scss'
import '../../scss/operate/operateStyle.scss'
import '../../scss/operate/operateWarehouse.scss'
import '../../scss/operate/operateMap.scss'
import React, { Component,PropTypes} from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import * as OperateActions from '../../actions/operate/OperateAction'
import * as MapActions from '../../actions/operate/MapAction'
import MenusBox from '../../components/operate/MenusBox'
import LayersBox from '../../components/operate/LayersBox'
import StyleBox from '../../components/operate/StyleBox'
import WarehouseBox from '../../components/operate/WarehouseBox'
import MapBox from '../../components/operate/MapBox'
import MapControl from '../../components/operate/MapControl'
import * as mapActions from '../../actions/operate/MapAction'
import env from '../../core/env'

class OperateBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)
        this.menuToggleAction = this.menuToggleAction.bind(this)
        this.panelDisplayInfoAction = this.panelDisplayInfoAction.bind(this)
    }

    componentDidMount() {
        this.props.operateActions.solutionInfoAction(this.props.params.id)
    }

    menuToggleAction(key) {
        let warehouseStyle = this.refs.operateWarehouseContainer.style
        let layerStyle = this.refs.operateLayerContainer.style
        let styleStyle = this.refs.operateStyleContainer.style
        let uploadStyle = this.refs.operateDataUploadContainer.style
        let mapStyle = this.refs.operateMapContainer.style

        switch (key) {
            case 'solution':
                styleStyle.display = 'none'
                warehouseStyle.display = 'none'
                if (layerStyle.display == 'block') {
                    layerStyle.display = 'none'
                    mapStyle.left = '60px'
                } else if (layerStyle.display == 'none') {
                    layerStyle.display = 'block'
                    mapStyle.left = '260px'
                }
                return layerStyle.display
            case 'warehouse':
                if (warehouseStyle.display == 'block') {
                    warehouseStyle.display = 'none'
                } else if (warehouseStyle.display == 'none') {
                    warehouseStyle.display = 'block'
                }
                return warehouseStyle.display
            case 'upload':
                if (uploadStyle.display == 'block') {
                    uploadStyle.display = 'none'
                } else if (uploadStyle.display == 'none') {
                    uploadStyle.display = 'block'
                }
                return uploadStyle.display
            case 'style':
                if (styleStyle.display == 'block') {
                    styleStyle.display = 'none'
                } else if (styleStyle.display == 'none') {
                    styleStyle.display = 'block'
                }
                return styleStyle.display
            default:
                return 'none'
        }
    }

    panelDisplayInfoAction(key, display = undefined) {
        let warehouseStyle = this.refs.operateWarehouseContainer.style
        let layerStyle = this.refs.operateLayerContainer.style
        let styleStyle = this.refs.operateStyleContainer.style
        let uploadStyle = this.refs.operateDataUploadContainer.style
        let mapStyle = this.refs.operateMapContainer.style

        switch (key) {
            case 'solution':
                if (display) {
                    layerStyle.display = display
                }
                return layerStyle.display
            case 'warehouse':
                if (display) {
                    warehouseStyle.display = display
                }
                return warehouseStyle.display
            case 'upload':
                if (display) {
                    uploadStyle.display = display
                }
                return uploadStyle.display
            case 'style':
                if (display) {
                    styleStyle.display = display
                }
                return styleStyle.display
            default:
                return 'none'
        }
    }

    render() {

        const bodyHeight = document.body.clientHeight - 0;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        const { operate,map,operateActions,mapActions} = this.props

        let operateDisplayInterface = <div className="page-body-container" style={bodyStyle}>
            <div className="operate-body">
                <div className="operate-menu-container" style={{display:'block'}} ref="operateMenuContainer">
                    <MenusBox solution={operate.solution} layerStyle={operate.layerStyle} actions={operateActions}
                              menuToggleAction={this.menuToggleAction}
                              mapInitStateChangeAction={mapActions.mapInitStateChangeAction}/>
                </div>
                <div className="operate-layer-container" style={{display:'block'}} ref="operateLayerContainer">
                    <LayersBox solution={operate.solution} layerStyle={operate.layerStyle} actions={operateActions}
                               menuToggleAction={this.menuToggleAction} mapActions={mapActions}
                               panelDisplayInfoAction={this.panelDisplayInfoAction}/>
                </div>
                <div className="operate-style-container" style={{display:'none'}} ref="operateStyleContainer">
                    <StyleBox solution={operate.solution} layerStyle={operate.layerStyle}
                              layerSourceAttrs={operate.layerSourceAttrs} actions={operateActions}
                              panelDisplayInfoAction={this.panelDisplayInfoAction}/>
                </div>
                <div className="operate-warehouse-container" style={{display:'none'}}
                     ref="operateWarehouseContainer">
                    <WarehouseBox solution={operate.solution} actions={operateActions} warehouse={operate.warehouse}
                                  panelDisplayInfoAction={this.panelDisplayInfoAction}/>
                </div>
                <div className="operate-dataupload-container" style={{display:'none'}}
                     ref="operateDataUploadContainer">
                </div>
                <div className="operate-map-container" style={{display:'block'}} ref="operateMapContainer">
                    <MapBox solution={operate.solution} actions={operateActions} map={map}
                        {...mapActions}/>
                    <MapControl solution={operate.solution} actions={operateActions} {...mapActions} map={map}/>
                </div>
            </div>
        </div>

        let displayInterface = <div className="operate-map-display-container" style={{display:'block'}}
                                    ref="operateMapContainer">
            <MapBox solution={operate.solution} actions={operateActions} map={map}
                {...mapActions}/>
            <MapControl solution={operate.solution} actions={operateActions} {...mapActions} map={map}/>
        </div>
        return <div>{operate.displayInterface == "display" ? displayInterface : operateDisplayInterface}</div>
    }
}

OperateBodyContainer.propTypes = {
    operate: PropTypes.object.isRequired,
    operateActions: PropTypes.object.isRequired
}

function mapStateToProps(state) {
    return {
        operate: state.operate,
        map: state.map
    }
}

function mapDispatchToProps(dispatch) {
    return {
        operateActions: bindActionCreators(OperateActions, dispatch),
        mapActions: bindActionCreators(MapActions, dispatch)
    }
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(OperateBodyContainer)

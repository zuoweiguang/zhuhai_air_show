import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'
import env from '../../core/env'

export default class MenusBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.onBackStudioClick = this.onBackStudioClick.bind(this)
        this.onSolutionClick = this.onSolutionClick.bind(this)
        this.onHelpClick = this.onHelpClick.bind(this)
    }

    onBackStudioClick(e) {
        env.map = null
        this.props.mapInitStateChangeAction(false, false)
        this.props.actions.solutionAllLayerSelectedAction(false)
        this.props.actions.layerStyleInfoUnselectedAction()
    }

    onSolutionClick(e) {
        const display = this.props.menuToggleAction('solution')
        this.refs.solutionMenu.className = 'menu-item'
        this.refs.helpMenu.className = 'menu-item'
        if (display == 'block') {
            this.refs.solutionMenu.className = 'menu-item selected'
        } else {
            this.props.actions.solutionAllLayerSelectedAction(false)
            this.props.actions.layerStyleInfoUnselectedAction()
        }
        let map = env.map
        if (map) {
            map.resize()
        }
    }

    onHelpClick(e) {
        this.refs.solutionMenu.className = 'menu-item'
        this.refs.helpMenu.className = 'menu-item selected'

        const display = this.props.menuToggleAction('help')
        this.refs.solutionMenu.className = 'menu-item'
        this.refs.helpMenu.className = 'menu-item'
        if (display == 'block') {
            this.refs.helpMenu.className = 'menu-item selected'
        }
    }

    render() {
        const { solution,actions,mapInitStateChangeAction} = this.props
        return <div className="operate-menu-box">
            <div className="operate-menu-list">
                <Link to={APP_ROOT_NAME+"studio"} onClick={this.onBackStudioClick}>
                    <div className="menu-main" title="返回工作台"
                         style={{height:'46px',paddingTop:'0px',paddingBottom:'0px'}}>
                        <div className="menu-content" style={{lineHeight:'46px'}}>
                            <div className="icon-back"/>
                        </div>
                    </div>
                </Link>
                <div className="menu-item selected" ref="solutionMenu" onClick={this.onSolutionClick}>
                    <div className="menu-content">
                        <div
                            className="icon-box icon-solution"></div>
                        <br/>
                        <span>方案</span>
                    </div>
                </div>
                <div className="menu-item" ref="helpMenu" onClick={this.onHelpClick}>
                    <div className="menu-content">
                        <div
                            className="icon-box icon-help"></div>
                        <br/>
                        <span>帮助</span>
                    </div>
                </div>
            </div>
        </div>
    }
}

MenusBox.propTypes = {
    solution: PropTypes.object.isRequired,
    actions: PropTypes.object.isRequired
}
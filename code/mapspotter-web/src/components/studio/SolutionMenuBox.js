import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'

export default class SolutionMenuBox extends Component {
    constructor(props, context) {
        super(props, context)
        this.onStudioMenuClick = this.onStudioMenuClick.bind(this)
    }

    onStudioMenuClick(e) {
        e.preventDefault()
        this.props.studioMenuLinkAction(this.props.link.id)
    }

    render() {
        const { link,studioMenuLinkAction} = this.props
        return <Link key={link.id} to={link.href} onClick={() => studioMenuLinkAction(link.id)}>
            <div className={"menu-item "+link.selected} ref={link.id+"Menu"}>
                <div className={"icon-box icon-"+link.id}/>
                <span className="menu-tip">{link.name}</span>
            </div>
        </Link>
    }
}
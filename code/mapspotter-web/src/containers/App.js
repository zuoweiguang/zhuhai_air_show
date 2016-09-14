import React, { Component,PropTypes } from 'react'
import '../scss/common/page.css'

export default class App extends Component {

    render() {
        const { headerView, bodyView } = this.props
        return (
            <div>
                <div ref="pageHeader">
                    {headerView}
                </div>
                <div ref="pageBody">
                    {bodyView}
                </div>
            </div>
        )
    }
}
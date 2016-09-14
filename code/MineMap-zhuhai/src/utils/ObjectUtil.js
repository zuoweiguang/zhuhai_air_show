/**
 * Given a destination object and optionally many source objects,
 * copy all properties from the source objects into the destination.
 * The last source object given overrides properties from previous
 * source objects.
 * @param {Object} dest destination object
 * @param {...Object} sources sources from which properties are pulled
 * @returns {Object} dest
 * @private
 */
export function extend(dest) {
    for (var i = 1; i < arguments.length; i++) {
        var src = arguments[i]
        for (var k in src) {
            dest[k] = src[k]
        }
    }
    return dest
}

/**
 * Extend a destination object with all properties of the src object,
 * using defineProperty instead of simple assignment.
 * @param {Object} dest
 * @param {Object} src
 * @returns {Object} dest
 * @private
 */
export function extendAll(dest, src) {
    for (var i in src) {
        Object.defineProperty(dest, i, Object.getOwnPropertyDescriptor(src, i))
    }
    return dest
}

/**
 * Extend a parent's prototype with all properties in a properties
 * object.
 *
 * @param {Object} parent
 * @param {Object} props
 * @returns {Object}
 * @private
 */
export function inherit (parent, props) {
    var parentProto = typeof parent === 'function' ? parent.prototype : parent,
        proto = Object.create(parentProto)
    extendAll(proto, props)
    return proto
}

/**
 * Create an object by mapping all the values of an existing object while
 * preserving their keys.
 * @param {Object} input
 * @param {Function} iterator
 * @returns {Object}
 * @private
 */
export function mapObject (input, iterator, context) {
    var output = {}
    for (var key in input) {
        output[key] = iterator.call(context || this, input[key], key, input)
    }
    return output
}

/**
 * Create an object by filtering out values of an existing object
 * @param {Object} input
 * @param {Function} iterator
 * @returns {Object}
 * @private
 */
export function filterObject (input, iterator, context) {
    var output = {};
    for (var key in input) {
        if (iterator.call(context || this, input[key], key, input)) {
            output[key] = input[key]
        }
    }
    return output
}

/**
 * Deeply clones two objects.
 * @param {Object} obj1
 * @param {Object} obj2
 * @returns {boolean}
 * @private
 */
export function deepClone(input) {
    if (Array.isArray(input)) {
        return input.map(deepClone)
    } else if (typeof input === 'object') {
        return mapObject(input, deepClone)
    } else {
        return input
    }
}




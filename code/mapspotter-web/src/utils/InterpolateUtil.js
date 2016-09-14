const InterpolateUtil = {
    interpolateNumber(input, base, inputLower, inputUpper, outputLower, outputUpper) {
        var difference = inputUpper - inputLower;
        var progress = input - inputLower;

        var ratio;
        if (base === 1) {
            ratio = progress / difference;
        } else {
            ratio = (Math.pow(base, progress) - 1) / (Math.pow(base, difference) - 1);
        }

        return (outputLower * (1 - ratio)) + (outputUpper * ratio);
    },
    interpolateArray(input, base, inputLower, inputUpper, outputLower, outputUpper) {
        var output = [];
        for (var i = 0; i < outputLower.length; i++) {
            output[i] = interpolateNumber(input, base, inputLower, inputUpper, outputLower[i], outputUpper[i]);
        }
        return output;
    }
}


export default InterpolateUtil

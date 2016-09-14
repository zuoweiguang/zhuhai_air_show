package com.navinfo.mapspotter.process.convert.road.transfer;

public class Transformation {
    // 设备的宽度和高度
    double m_DeviceWidth;
    double m_DeviceHeight;
    // 世界坐标的原点 宽度高度
    double m_WorldOriginX;
    double m_WorldOriginY;
    double m_WorldWidth;
    double m_WorldHeight;

    double[] m_WORLD_MIN = new double[2];

    double[] m_WORLD_SIZE = new double[2];

    double[][] m_WORLD_PAGE = new double[3][3];

    double[][] m_PAGE_WORLD = new double[3][3];

    double m_ScaleRatio;

    double[] m_MMScale = new double[2];

    public Transformation() {
        m_DeviceHeight = m_DeviceWidth = 0;

        m_WorldOriginX = m_WorldOriginY = m_WorldWidth = m_WorldHeight = 0;

        m_MMScale[0] = Double.MIN_VALUE;

        m_MMScale[1] = Double.MAX_VALUE;
    }

    private void UPDATE_W2P_MATRIX() {
        double width_scale = m_DeviceWidth / m_WorldWidth;

        double height_scale = m_DeviceHeight / m_WorldHeight;

        double scale = width_scale < height_scale ? width_scale : height_scale;

        double center_x = m_WorldOriginX + m_WorldWidth / 2;

        double center_y = m_WorldOriginY + m_WorldHeight / 2;

        if (scale > m_MMScale[1]) {
            scale = m_MMScale[1];
        }
        if (scale < m_MMScale[0]) {
            scale = m_MMScale[0];
        }

        double width_of_extent = m_DeviceWidth / scale;

        double height_of_extent = m_DeviceHeight / scale;

        m_WORLD_MIN[0] = (center_x - width_of_extent / 2);

        m_WORLD_MIN[1] = (center_y - height_of_extent / 2);

        m_WORLD_SIZE[0] = width_of_extent;

        m_WORLD_SIZE[1] = height_of_extent;

        m_WORLD_PAGE[0][0] = scale;

        m_WORLD_PAGE[1][0] = 0;

        m_WORLD_PAGE[2][0] = -m_WORLD_MIN[0] * scale;

        m_WORLD_PAGE[0][1] = 0;

        m_WORLD_PAGE[1][1] = -scale;

        m_WORLD_PAGE[2][1] = m_DeviceHeight + m_WORLD_MIN[1] * scale;

        m_WORLD_PAGE[0][2] = 0;

        m_WORLD_PAGE[1][2] = 0;

        m_WORLD_PAGE[2][2] = 0;

        m_ScaleRatio = scale;
    }

    private void UPDATE_P2W_MATRIX() {
        double scale = 1 / m_ScaleRatio;

        m_PAGE_WORLD[0][0] = scale;

        m_PAGE_WORLD[1][0] = 0;

        m_PAGE_WORLD[2][0] = m_WORLD_MIN[0];

        m_PAGE_WORLD[0][1] = 0;

        m_PAGE_WORLD[1][1] = -scale;

        m_PAGE_WORLD[2][1] = m_WORLD_MIN[1] + m_WORLD_SIZE[1];

        m_PAGE_WORLD[0][2] = 0;

        m_PAGE_WORLD[1][2] = 0;

        m_PAGE_WORLD[2][2] = 0;
    }

    private void UPDATE_MATRIX() {
        UPDATE_W2P_MATRIX();

        UPDATE_P2W_MATRIX();
    }

    /**
     * @param x 坐标的原点[墨卡托坐标]
     * @param y 坐标的原点[墨卡托坐标]
     * @param w 宽度[通过墨卡托坐标获取bound，然后计算高度和宽度]
     * @param h 高度
     */
    public void setWorldBounds(double x, double y, double w, double h) {
        m_WorldOriginX = x;

        m_WorldOriginY = y;

        m_WorldWidth = w;

        m_WorldHeight = h;

        UPDATE_MATRIX();
    }

    /**
     * @param w 图片宽度
     * @param h 图片高度
     */
    public void setDeviceFrame(double w, double h) {
        m_DeviceWidth = w;

        m_DeviceHeight = h;
    }

    /**
     * 把坐标转化成设备的像素坐标
     *
     * @param x
     * @param y
     * @return
     */
    public double[] transformDevice(double x, double y) {
        double _x = x * m_WORLD_PAGE[0][0] + m_WORLD_PAGE[2][0];

        double _y = y * m_WORLD_PAGE[1][1] + m_WORLD_PAGE[2][1];

        return new double[]{_x, _y};
    }

}

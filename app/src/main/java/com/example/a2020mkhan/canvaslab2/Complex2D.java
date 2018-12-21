package com.example.a2020mkhan.canvaslab2;
public class Complex2D {

    public double real[][];
    public double imag[][];

    private int n_x;
    private int n_y;


    // DEFAULT CONSTRUCTOR

    public Complex2D() {
    }

    public Complex2D(int n_rows, int n_cols) {
        this.n_x = n_rows;
        this.n_y = n_cols;

        this.real = new double[n_rows][n_cols];
        this.imag = new double[n_rows][n_cols];
    }

    public void assign(double[][] values_real) {
        for (int i=0; i<values_real.length; i++) {
            for (int j=0; j<values_real[0].length; j++) {
                this.real[i][j] = values_real[i][j];
            }
        }
    }

    public void assign(double[][] values_real, double[][] values_imag) {
        for (int i=0; i<values_real.length; i++) {
            for (int j=0; j<values_real[0].length; j++) {
                this.real[i][j] = values_real[i][j];
                this.imag[i][j] = values_imag[i][j];
            }
        }
    }

    public void assignFrom(double[] values_real, int stride) {
        int i = 0;
        int j;
        int k = 0;

        while( k< values_real.length) {

            for (j=0; j<stride; j++) {
                this.real[i][j] = values_real[k++];
            }
            i++;
        }
    }

    public void assignFrom(double[] values_real, int stride, double others) {
        int i = 0;
        int I, j, J;
        int k = 0;

        while( k< values_real.length) {

            for (j=0; j<stride; j++) {
                this.real[i][j] = values_real[k++];
                this.imag[i][j] = others;
            }
            for (J=j; J<n_y; J++) {
                this.real[i][J] = others;
                this.imag[i][J] = others;
            }
            i++;
        }
        for(I=i; I<n_x; I++) {
            for (J=0; J<n_y; J++) {
                this.real[I][J] = others;
                this.imag[I][J] = others;
            }
        }
    }

    public void complexMultiply(double[][] scale_r, double[][] scale_i ){
        double tr, ti;

        for(int i=0; i<n_x; i++) {
            for(int j=0; j<n_y; j++) {
                tr = this.real[i][j] * scale_r[i][j] - this.imag[i][j]*scale_i[i][j];
                ti = this.real[i][j] * scale_i[i][j] + this.imag[i][j]*scale_r[i][j];

                this.real[i][j] = tr;
                this.imag[i][j] = ti;
            }
        }
    }

}

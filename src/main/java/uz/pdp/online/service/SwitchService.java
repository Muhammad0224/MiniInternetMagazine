package uz.pdp.online.service;

public interface SwitchService {
    static int operator(int operator, int size) {
        if (operator >= 0 && operator <= size ) {
            return operator;
        } else return -1;
    }
}

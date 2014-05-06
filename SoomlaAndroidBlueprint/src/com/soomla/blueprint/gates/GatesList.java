package com.soomla.blueprint.gates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by refaelos on 06/05/14.
 */
public abstract class GatesList extends ArrayList<Gate> {
    public abstract List<Gate> canPass();
}

package ibis.deploy.library.gui;

import android.view.View;

public interface SelectionComponent {

    public View getView();

    public Object[] getValues();

    public void update();

}
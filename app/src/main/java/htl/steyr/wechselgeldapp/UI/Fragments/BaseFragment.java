package htl.steyr.wechselgeldapp.UI.Fragments;

import androidx.fragment.app.Fragment;

/**
 * BaseFragment is an abstract base class for all UI fragments.
 * It requires subclasses to provide a title that can be used
 * in the user interface (e.g., in the header or toolbar).
 */
public abstract class BaseFragment extends Fragment {

    /**
     * Returns the title for the current fragment.
     * This is typically shown in the app's header or navigation bar.
     *
     * @return a short descriptive title
     */
    public abstract String getTitle();
}

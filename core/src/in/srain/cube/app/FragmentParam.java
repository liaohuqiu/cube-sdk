package in.srain.cube.app;


public class FragmentParam {

	enum TYPE {
		ADD, REPLACE
	};

	public CubeFragment from;
	public Class<?> cls;
	public Object data;
	public TYPE type = TYPE.ADD;
	public boolean addToBackStack = true;
}
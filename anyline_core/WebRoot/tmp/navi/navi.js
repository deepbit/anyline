function _navi_go(n) {
	if (!n) {
		n = document.getElementById('_anyline_go').value;
	}
	var frm = document.forms["_navi_frm"];
	if (frm) {
		if(frm._anyline_page.value == n){
			return;
		}
		frm._anyline_page.value = n;

		if( typeof changePgae === 'function' ){
			changePgae(n);
		}else{			
			frm.submit();
		}
	}
}
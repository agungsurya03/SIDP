/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import model.fresh.*;
import model.lazy.LazyTka;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Raihan
 */
public class TkaBean {

    private static final long serialVersionUID = -1005091770846750945L;

    private BackendBrain backendBrain;

    boolean called = false; //nasty bug. prerenderView called multiple times. Use this.
    private Tka tka; // untuk edit mode
    private Tka tkaClone; // untuk list mode
    private ArrayList<Tka> tkas;

    // Lazy Model
    private LazyTka lazyTkas;

    //hashmap untuk combobox
    private Map<String, String> pendidikan;
    private Map<String, String> jenisKelamins;
    private Map<String, String> jenises;
    private Map<String, String> jurusans;

    //untuk show/hide kolom di list mode
    private boolean showIdTka = true;
    private boolean showIdKkks;
    private boolean showNamaKkks;
    private boolean showNip = true;
    private boolean showNamaTkaAwal = true;
    private boolean shownamaTkaTengah = true;
    private boolean showNamaTkaAkhir = true;
    private boolean showAlias = true;
    private boolean showAlamat;
    private boolean showWarganegara = true;
    private boolean showTempatLahir;
    private boolean showTanggalLahir;
    private boolean showJenisKelamin;
    private boolean showNomorPassport = true;
    private boolean showPendidikan1 ;
    private boolean showJenis1;
    private boolean showKetPendidikan1;
    private boolean showPendidikan2;
    private boolean showJenis2;
    private boolean showKetPendidikan2;
    private boolean showPendidikan3;
    private boolean showJenis3;
    private boolean showKetPendidikan3;
    private boolean showPengalaman1;
    private boolean showPengalaman2;
    private boolean showPengalaman3;
    private boolean showPengalaman4;
    private boolean showPengalaman5;
    private boolean showTahunPengalaman;
    private boolean showKetTambahan;
    private boolean showStatus;
    private List<String> listStatus = new ArrayList<String>(Arrays.asList("Aktif", "Non-Aktif"));

    private Kkks kkks;

    Map<String, Object> pickerOptions;
    private LazyDataModel<Tka> lazyModel;
    private ArrayList<Tka> selectedTkas;
    private StreamedContent file;

    private String nipnow;

    private int indexStatusTemp;
    private String valueStatusTemp;
    private ArrayList<Tka> tempTkas;

    public BackendBrain getBackendBrain() {
        return backendBrain;
    }

    public void setBackendBrain(BackendBrain backendBrain) {
        this.backendBrain = backendBrain;
    }

    public void showMessage(String title, String alerton) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, title, alerton);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    //untuk show/hide kolom di list mode
    public TkaBean() {
        InputStream stream = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getResourceAsStream("/resources/tka.xlsx");
        file = new DefaultStreamedContent(stream, "xls", "template_tka.xlsx");
    }

    @PostConstruct
    public void fillTools() {
        pendidikan = new HashMap<String, String>();
        pendidikan.put("S1", "S1");
        pendidikan.put("S2", "S2");
        pendidikan.put("S3", "S3");
        pendidikan.put("D3", "D3");
        jenisKelamins = new HashMap<String, String>();
        jenisKelamins.put("Pria", "Pria");
        jenisKelamins.put("Wanita", "Wanita");
        jenises = new HashMap<String, String>();
        jenises.put("Teknis", "Teknis");
        jenises.put("Non-Teknis", "Non-Teknis");
        // TODO: Buat jadi ngambil dari table master
        jurusans = new HashMap<String, String>();
        BackendBrain bb = new BackendBrain();
        ArrayList<RJurusan> listJurusan = (ArrayList<RJurusan>) bb.listJurusan();
        for (RJurusan jurusan : listJurusan) {
            jurusans.put(jurusan.getName(), jurusan.getName());
        }
        pickerOptions = new HashMap<String, Object>();
        pickerOptions.put("modal", true);
        pickerOptions.put("draggable", false);
        pickerOptions.put("resizable", false);
    }

    public void loadOnce(int idKkks) {
        if (!called && idKkks > 0) {
            fetchCollection(idKkks);
//            called = true;
        }
    }

    public void fetchCollection(int idKkks) {
        lazyTkas = new LazyTka();
        lazyTkas.setIdKkks(idKkks);
        tkas = backendBrain.fetchTkasFK(idKkks);
        //tkas = new ArrayList<Tka>();
//        if (idKkks > -1) {
//            tkas.add(new Tka(0));
//        }
    }

    public void fetchOne(int idTka, int filterKkks) {
        if (!called) {
            if (idTka > 0) {
                tka = backendBrain.fetchTkaPK(idTka);
                int year = Calendar.getInstance().get(Calendar.YEAR);
                if(tka.getTahunBekerja() == null) {
                    tka.setTahunPengalaman("");
                } else {
                    tka.setTahunPengalaman("" + (year - tka.getTahunBekerja().intValue()));
                }
                kkks = backendBrain.fetchKkksPK(tka.getKkks());
                selectedTkas = new ArrayList<Tka>();
                selectedTkas.add(tka);
                if (tka.getNip() != null) {
                    String[] split = tka.getNip().split("-");
                    tka.setNip(split[1]);
                }
                nipnow = tka.getNip();
            } else {
                tka = new Tka();
                if (filterKkks > -1) {
                    kkks = backendBrain.fetchKkksPK(filterKkks);
                    tka.setKkks(filterKkks);
                }
            }
            called = true;
        }
    }

    public String delete() {
        for (Tka selectedTka : selectedTkas) {
            backendBrain.deleteTkaPK(selectedTka.getIdTka());
        }
        return "list";
    }

    public ArrayList<String> checkform(boolean isEdit) {
        ArrayList<String> formDone = new ArrayList<String>();
        if (tka.getKkks() < 1) {
            formDone.add("KKKS tidak boleh kosong");
        } else {
            if (!tka.getNip().equals(nipnow)) {
                Tka tmp = backendBrain.fetchTkaNipPK(tka.getKkks() + "-" + tka.getNip());
                if (tmp != null && tmp.getNip() != null) {
                    formDone.add("NIP sudah terdaftar di sistem");
                }
            }
        }
        if (tka.getAlias() != null) {
            if (tka.getAlias().length() > 16) {
                formDone.add("Nama alias tidak boleh lebih dari 16 karakter");
            }
        }
        return formDone;
    }

    public ArrayList<String> saveThis() {
        ArrayList<String> formDone = new ArrayList<String>();
        formDone = checkform(true);
        if (formDone.isEmpty()) {
            nipnow = tka.getNip();
            tka.setNip(tka.getKkks() + "-" + nipnow);
            backendBrain.updateTkaPK(tka);
            tka.setNip(nipnow);
        }
        return formDone;
    }

    public ArrayList<String> insert() {
        ArrayList<String> formDone = new ArrayList<String>();
        formDone = checkform(false);
        if (formDone.isEmpty()) {
            nipnow = tka.getNip();
            tka.setNip(tka.getKkks() + "-" + nipnow);
            int id = backendBrain.insertTka(tka);
            tka.setIdTka(id);
            selectedTkas = new ArrayList<Tka>();
            selectedTkas.add(tka);
            tka.setNip(nipnow);
        }
        return formDone;
    }

    public String cancelInsert() {
        return "list";
    }

    public void backup() {
        tkaClone = new Tka();
        tkaClone.backup(tka);
    }

    public String discardThis() {
        tka.backup(tkaClone);
        return "back";
    }

    public void openKkksPicker() {
        RequestContext.getCurrentInstance().openDialog("dialog/kkksPicker", pickerOptions, null);
    }

    public void pickKkks(SelectEvent event) {
        Kkks kkksPick = (Kkks) event.getObject();
        tka.setKkks(kkksPick.getIdKkks());
        kkks = kkksPick;
    }

    public void clearKkksPick() {
        tka.setKkks(-1);
        kkks = null;
    }

    public Tka getTka() {
        return tka;
    }

    public void setTka(Tka tka) {
        this.tka = tka;
    }

    public Tka getTkaClone() {
        return tkaClone;
    }

    public void setTkaClone(Tka tkaClone) {
        this.tkaClone = tkaClone;
    }

    public ArrayList<Tka> getTkas() {
        return tkas;
    }

    public void setTkas(ArrayList<Tka> tkas) {
        this.tkas = tkas;
    }

    public Map<String, String> getPendidikan() {
        return pendidikan;
    }

    public void setPendidikan(Map<String, String> pendidikan) {
        this.pendidikan = pendidikan;
    }

    public Map<String, String> getJenisKelamins() {
        return jenisKelamins;
    }

    public void setJenisKelamins(Map<String, String> jenisKelamins) {
        this.jenisKelamins = jenisKelamins;
    }

    public Map<String, String> getJenises() {
        return jenises;
    }

    public void setJenises(Map<String, String> jenises) {
        this.jenises = jenises;
    }

    public boolean isShowIdTka() {
        return showIdTka;
    }

    public void setShowIdTka(boolean showIdTka) {
        this.showIdTka = showIdTka;
    }

    public boolean isShowIdKkks() {
        return showIdKkks;
    }

    public void setShowIdKkks(boolean showIdKkks) {
        this.showIdKkks = showIdKkks;
    }

    public boolean isShowNamaKkks() {
        return showNamaKkks;
    }

    public void setShowNamaKkks(boolean showNamaKkks) {
        this.showNamaKkks = showNamaKkks;
    }

    public boolean isShowNip() {
        return showNip;
    }

    public void setShowNip(boolean showNip) {
        this.showNip = showNip;
    }

    public boolean isShowNamaTkaAwal() {
        return showNamaTkaAwal;
    }

    public void setShowNamaTkaAwal(boolean showNamaTkaAwal) {
        this.showNamaTkaAwal = showNamaTkaAwal;
    }

    public boolean isShownamaTkaTengah() {
        return shownamaTkaTengah;
    }

    public void setShownamaTkaTengah(boolean shownamaTkaTengah) {
        this.shownamaTkaTengah = shownamaTkaTengah;
    }

    public boolean isShowNamaTkaAkhir() {
        return showNamaTkaAkhir;
    }

    public void setShowNamaTkaAkhir(boolean showNamaTkaAkhir) {
        this.showNamaTkaAkhir = showNamaTkaAkhir;
    }

    public boolean isShowAlias() {
        return showAlias;
    }

    public void setShowAlias(boolean showAlias) {
        this.showAlias = showAlias;
    }

    public boolean isShowAlamat() {
        return showAlamat;
    }

    public void setShowAlamat(boolean showAlamat) {
        this.showAlamat = showAlamat;
    }

    public boolean isShowWarganegara() {
        return showWarganegara;
    }

    public void setShowWarganegara(boolean showWarganegara) {
        this.showWarganegara = showWarganegara;
    }

    public boolean isShowTempatLahir() {
        return showTempatLahir;
    }

    public void setShowTempatLahir(boolean showTempatLahir) {
        this.showTempatLahir = showTempatLahir;
    }

    public boolean isShowTanggalLahir() {
        return showTanggalLahir;
    }

    public void setShowTanggalLahir(boolean showTanggalLahir) {
        this.showTanggalLahir = showTanggalLahir;
    }

    public boolean isShowJenisKelamin() {
        return showJenisKelamin;
    }

    public void setShowJenisKelamin(boolean showJenisKelamin) {
        this.showJenisKelamin = showJenisKelamin;
    }

    public boolean isShowPendidikan1() {
        return showPendidikan1;
    }

    public void setShowPendidikan1(boolean showPendidikan1) {
        this.showPendidikan1 = showPendidikan1;
    }

    public boolean isShowJenis1() {
        return showJenis1;
    }

    public void setShowJenis1(boolean showJenis1) {
        this.showJenis1 = showJenis1;
    }

    public boolean isShowKetPendidikan1() {
        return showKetPendidikan1;
    }

    public void setShowKetPendidikan1(boolean showKetPendidikan1) {
        this.showKetPendidikan1 = showKetPendidikan1;
    }

    public boolean isShowPendidikan2() {
        return showPendidikan2;
    }

    public void setShowPendidikan2(boolean showPendidikan2) {
        this.showPendidikan2 = showPendidikan2;
    }

    public boolean isShowJenis2() {
        return showJenis2;
    }

    public void setShowJenis2(boolean showJenis2) {
        this.showJenis2 = showJenis2;
    }

    public boolean isShowKetPendidikan2() {
        return showKetPendidikan2;
    }

    public void setShowKetPendidikan2(boolean showKetPendidikan2) {
        this.showKetPendidikan2 = showKetPendidikan2;
    }

    public boolean isShowPendidikan3() {
        return showPendidikan3;
    }

    public void setShowPendidikan3(boolean showPendidikan3) {
        this.showPendidikan3 = showPendidikan3;
    }

    public boolean isShowJenis3() {
        return showJenis3;
    }

    public void setShowJenis3(boolean showJenis3) {
        this.showJenis3 = showJenis3;
    }

    public boolean isShowKetPendidikan3() {
        return showKetPendidikan3;
    }

    public void setShowKetPendidikan3(boolean showKetPendidikan3) {
        this.showKetPendidikan3 = showKetPendidikan3;
    }

    public boolean isShowPengalaman1() {
        return showPengalaman1;
    }

    public void setShowPengalaman1(boolean showPengalaman1) {
        this.showPengalaman1 = showPengalaman1;
    }

    public boolean isShowPengalaman2() {
        return showPengalaman2;
    }

    public void setShowPengalaman2(boolean showPengalaman2) {
        this.showPengalaman2 = showPengalaman2;
    }

    public boolean isShowPengalaman3() {
        return showPengalaman3;
    }

    public void setShowPengalaman3(boolean showPengalaman3) {
        this.showPengalaman3 = showPengalaman3;
    }

    public boolean isShowPengalaman4() {
        return showPengalaman4;
    }

    public void setShowPengalaman4(boolean showPengalaman4) {
        this.showPengalaman4 = showPengalaman4;
    }

    public boolean isShowPengalaman5() {
        return showPengalaman5;
    }

    public void setShowPengalaman5(boolean showPengalaman5) {
        this.showPengalaman5 = showPengalaman5;
    }

    public boolean isShowKetTambahan() {
        return showKetTambahan;
    }

    public void setShowKetTambahan(boolean showKetTambahan) {
        this.showKetTambahan = showKetTambahan;
    }

    public Kkks getKkks() {
        return kkks;
    }

    public void setKkks(Kkks kkks) {
        this.kkks = kkks;
    }

    public ArrayList<Tka> getSelectedTkas() {
        return selectedTkas;
    }

    public void setSelectedTkas(ArrayList<Tka> selectedTkas) {
        this.selectedTkas = selectedTkas;
    }

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }

    public LazyDataModel<Tka> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<Tka> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public Map<String, String> getJurusans() {
        TreeMap<String, String> temp = new TreeMap<String, String>();
        temp.putAll(jurusans);
        return temp;
    }

    public void setJurusans(Map<String, String> jurusans) {
        this.jurusans = jurusans;
    }

    public boolean isShowTahunPengalaman() {
        return showTahunPengalaman;
    }

    public void setShowTahunPengalaman(boolean showTahunPengalaman) {
        this.showTahunPengalaman = showTahunPengalaman;
    }

    public boolean isShowNomorPassport() {
        return showNomorPassport;
    }

    public void setShowNomorPassport(boolean showNomorPassport) {
        this.showNomorPassport = showNomorPassport;
    }

    /**
     * @return the showStatus
     */
    public boolean isShowStatus() {
        return showStatus;
    }

    /**
     * @param showStatus the showStatus to set
     */
    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    /**
     * @return the listStatus
     */
    public List<String> getListStatus() {
        return listStatus;
    }

    /**
     * @param listStatus the listStatus to set
     */
    public void setListStatus(List<String> listStatus) {
        this.listStatus = listStatus;
    }

    /**
     * @return the indexStatusTemp
     */
    public int getIndexStatusTemp() {
        return indexStatusTemp;
    }

    /**
     * @param indexStatusTemp the indexStatusTemp to set
     */
    public void setIndexStatusTemp(int indexStatusTemp) {
        this.indexStatusTemp = indexStatusTemp;
    }

    /**
     * @return the valueStatusTemp
     */
    public String getValueStatusTemp() {
        return valueStatusTemp;
    }

    /**
     * @param valueStatusTemp the valueStatusTemp to set
     */
    public void setValueStatusTemp(String valueStatusTemp) {
        this.valueStatusTemp = valueStatusTemp;
    }

    /**
     * @return the tempTkas
     */
    public ArrayList<Tka> getTempTkas() {
        return tempTkas;
    }

    /**
     * @param tempTkas the tempTkas to set
     */
    public void setTempTkas(ArrayList<Tka> tempTkas) {
        this.tempTkas = tempTkas;
    }

    public void updateStatusTemp(int index, String value, int idKkks) {
        tempTkas = backendBrain.fetchTkasFK(idKkks);
        this.indexStatusTemp = index;
        this.valueStatusTemp = value;
    }

    public void updateStatus() {
        backendBrain.updateStatusTKA(this.indexStatusTemp, this.valueStatusTemp);
    }

    public void cancelUpdateStatus() {
        tkas = tempTkas;
    }

    public LazyTka getLazyTkas() {
        return lazyTkas;
    }

    public void setLazyTkas(LazyTka lazyTkas) {
        this.lazyTkas = lazyTkas;
    }
}

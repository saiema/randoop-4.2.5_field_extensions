Patching Defects4J to add Randoop v4.2.5 with Field Coverage support

This instructions assume that you followed the instructions for Randoop v4.2.5 with Field Coverage support, and that
you have that version of Randoop in directory **RandoopRoot**.


Start by cloning **Defects4J** into a directory of your choosing, which we will refer to as **Defects4JRoot**.
You can use the following command to do this, replacing **Defects4JRoot** with the directory you chose:
```Console
git clone git@github.com:rjust/defects4j.git Defects4JRoot
```

Copy the accompanying patch `defects4j_with_randoop-4.2.5fc.patch` file into directory **Defects4JRoot**, replace `<randoop_v4.2.5fc_root>` inside **defects4j_with_randoop-4.2.5fc.patch** with **RandoopRoot**, and apply the patch file by executing:
```Console
git apply defects4j_with_randoop-4.2.5fc.patch
```
*This must be done inside folder __Defects4JRoot__*

You will then need to continue with **Defects4J** original installation instructions:
```Console
sudo cpanm --installdeps .
./init.sh
```

You should then add `export PATH=$PATH:`**Defects4JRoot**`/framework/bin` to the appropiate file so it is kept for all terminal sessions,
`~/.profile` is one such file, but you should consult the appropiate one for your specific OS.

After the `./init.sh` script finishes, there should be a file **framework/lib/test_generation/generation/randoop-4.2.5/field_coverage_metrics.env**, you can use this file to configure the options for Field Coverage Metricts in **Randoop v4.2.5 fc**.

Options in **field_coverage_metricts.env** are documented, but the **OUTPUT** option is a bit complex, so here you have a more thorough explanation:

The structure for the values of this optionis `{"value":"<path>","as_path":"(true|false)"}`, if **as_path** is `true`, then **path** will be used as a path to a file; if **as_path** is `false`, then **path** can either be `stdout` to print to *Standard Output*, or `stderr` to print to *Standard Error*.

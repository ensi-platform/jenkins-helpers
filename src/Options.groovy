class Options {
    Script script

    def run() {
        script.echo("banana")
        script.echo(script.env)
    }
}
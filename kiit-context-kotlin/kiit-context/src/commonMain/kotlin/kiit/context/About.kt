package kiit.context

/**
 * Describes an application: the prose complement to [Identity]'s compact structural form.
 *
 * @param area Group owning the app.
 * @param name Name of the app.
 * @param desc Description of the app.
 * @param company Company the app is associated with.
 * @param region Region associated with the app.
 * @param url Url for more information.
 * @param contact Contact person(s) for the app.
 * @param tags Tags describing the app.
 */
data class About(
    val company: String = "",
    val area: String = "",
    val name: String,
    val desc: String,
    val region: String = "",
    val url: String = "",
    val contact: String = "",
    val tags: String = "",
    val examples: String = "",
) {
    val id: String = "$area.$name"

    fun log(callback: (String, String) -> Unit) {
        callback("company", company)
        callback("area", area)
        callback("name", name)
        callback("desc", desc)
        callback("region", region)
        callback("url", url)
        callback("contact", contact)
        callback("tags", tags)
        callback("examples", examples)
    }

    fun toStringProps(): String {
        return "company  : $company\n" +
            "area     : $area\n" +
            "name     : $name\n" +
            "desc     : $desc\n" +
            "region   : $region\n" +
            "url      : $url\n" +
            "contact  : $contact\n" +
            "tags     : $tags\n" +
            "examples : $examples\n"
    }

    fun dir(): String = company.ifBlank { name }.replace(" ", "-")

    /** Converts this into the equivalent structural [Identity], as [Agent.App] in "dev". */
    fun toId(): Identity = Identity.of(company, area, name, Agent.App, "dev")

    companion object {
        val none =
            About(
                company = "",
                area = "",
                name = "",
                desc = "",
                region = "",
                url = "",
                contact = "",
                tags = "",
                examples = "",
            )

        /** Builds an [About] using just the parameters supplied, everything else blank. */
        fun simple(
            company: String,
            area: String,
            name: String,
            desc: String
        ): About = About(company, area, name, desc, "", "", "", "", "")
    }
}
